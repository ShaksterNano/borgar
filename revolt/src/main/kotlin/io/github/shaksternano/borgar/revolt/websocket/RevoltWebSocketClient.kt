package io.github.shaksternano.borgar.revolt.websocket

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.github.shaksternano.borgar.core.io.httpClient
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.JSON
import io.github.shaksternano.borgar.messaging.event.MessageReceiveEvent
import io.github.shaksternano.borgar.messaging.util.onMessageReceived
import io.github.shaksternano.borgar.revolt.RevoltManager
import io.github.shaksternano.borgar.revolt.entity.RevoltGuildResponse
import io.github.shaksternano.borgar.revolt.entity.channel.RevoltChannelResponse
import io.github.shaksternano.borgar.revolt.entity.channel.RevoltChannelType
import io.github.shaksternano.borgar.revolt.entity.createMessage
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.util.network.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val REVOLT_WEBSOCKET_URL = "ws.revolt.chat"

private val PING_JSON: String =
    JsonObject(mapOf(
        "type" to JsonPrimitive("Ping"),
        "data" to JsonPrimitive(0),
    )).toString()
private val PING_INTERVAL: Duration = 10.seconds

private val RETRY_CONNECT_INTERVAL: Duration = 10.seconds

class RevoltWebSocketClient(
    private val token: String,
    private val manager: RevoltManager,
) {

    private val guildCountAtomic: AtomicInteger = AtomicInteger(0)
    val guildCount: Int
        get() = guildCountAtomic.get()
    private var session: DefaultClientWebSocketSession? = null
    private val messageHandlers: MutableMap<String, MutableList<WebSocketMessageHandler>> = mutableMapOf()
    private var ready: Boolean = false
    private var invalidToken: Boolean = false
    private var open: Boolean = true

    init {
        registerHandlers()

        // Create a custom dispatcher to use non-daemon threads
        val threadCount = Runtime.getRuntime().availableProcessors()
        val threadFactory = ThreadFactoryBuilder().setNameFormat("revolt-websocket-%d").build()
        val threadPool = Executors.newFixedThreadPool(threadCount, threadFactory)
        val dispatcher = threadPool.asCoroutineDispatcher()
        CoroutineScope(dispatcher).launch {
            httpClient {
                install(WebSockets)
            }.use { client ->
                runCatching {
                    while (open) {
                        try {
                            client.webSocket(
                                host = REVOLT_WEBSOCKET_URL,
                                path = "?version=1&format=json&token=$token",
                                request = {
                                    url {
                                        protocol = URLProtocol.WSS
                                    }
                                },
                            ) {
                                session = this
                                val pingJob = launch {
                                    sendPings()
                                }
                                handleMessages()
                                pingJob.cancel()
                            }
                            session = null
                            logger.info("Disconnected from Revolt WebSocket, reconnecting...")
                        } catch (e: UnresolvedAddressException) {
                            session = null
                            logger.info("Failed to connect to Revolt WebSocket, trying again in $RETRY_CONNECT_INTERVAL")
                            delay(RETRY_CONNECT_INTERVAL)
                        }
                    }
                }.onFailure {
                    logger.error("Error with Revolt WebSocket", it)
                }
            }
            session = null
        }
    }

    suspend fun awaitReady() {
        if (ready) return
        if (invalidToken) throw IllegalArgumentException("Invalid token")
        var resumed = false
        suspendCoroutine { continuation ->
            handle(WebSocketMessageType.READY) {
                if (resumed) return@handle
                resumed = true
                continuation.resume(Unit)
            }
            handle(WebSocketMessageType.NOT_FOUND) {
                if (resumed) return@handle
                resumed = true
                continuation.resumeWithException(IllegalArgumentException("Invalid token"))
            }
        }
    }

    suspend fun sendTyping(channelId: String) {
        session?.send(
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("BeginTyping"),
                    "channel" to JsonPrimitive(channelId),
                )
            ).toString()
        )
    }

    suspend fun stopTyping(channelId: String) {
        session?.send(
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive("EndTyping"),
                    "channel" to JsonPrimitive(channelId),
                )
            ).toString()
        )
    }

    private fun registerHandlers() {
        handle(WebSocketMessageType.AUTHENTICATED) {
            logger.info("Connected to Revolt")
        }
        handle(WebSocketMessageType.READY) {
            ready = true
            val body = JSON.decodeFromJsonElement(ReadyBody.serializer(), it)
            val guildCount = body.guilds.size
            val groupCount = body.channels.count { response ->
                response.type == RevoltChannelType.GROUP.apiName
            }
            guildCountAtomic.set(guildCount + groupCount)
        }
        handle(WebSocketMessageType.NOT_FOUND) {
            invalidToken = true
            open = false
        }
        handle(WebSocketMessageType.MESSAGE) {
            handleMessage(it)
        }
        handle(WebSocketMessageType.SERVER_CREATE) {
            guildCountAtomic.incrementAndGet()
        }
        handle(WebSocketMessageType.SERVER_DELETE) {
            guildCountAtomic.decrementAndGet()
        }
        handle(WebSocketMessageType.SERVER_MEMBER_LEAVE) {
            val userId = it["user"] as? JsonPrimitive
            if (userId?.content == manager.selfId) {
                guildCountAtomic.decrementAndGet()
            }
        }
        handle(WebSocketMessageType.CHANNEL_CREATE) {
            val channelType = it["channel_type"] as? JsonPrimitive
            if (channelType?.content == "Group") {
                guildCountAtomic.incrementAndGet()
            }
        }
        handle(WebSocketMessageType.CHANNEL_GROUP_LEAVE) {
            val userId = it["user"] as? JsonPrimitive
            if (userId?.content == manager.selfId) {
                guildCountAtomic.decrementAndGet()
            }
        }
    }

    private suspend fun handleMessage(json: JsonObject) {
        val message = createMessage(json, manager)
        val event = MessageReceiveEvent(message)
        onMessageReceived(event)
    }

    private fun handle(messageType: WebSocketMessageType, handler: suspend (JsonObject) -> Unit) {
        messageHandlers.getOrPut(messageType.apiName, ::mutableListOf)
            .add(WebSocketMessageHandler(handler))
    }

    private suspend fun WebSocketSession.sendPings() {
        while (open) {
            send(PING_JSON)
            delay(PING_INTERVAL)
        }
    }

    private suspend fun WebSocketSession.handleMessages() {
        if (!open) {
            incoming.cancel()
            return
        }
        coroutineScope {
            for (message in incoming) {
                if (!open) {
                    incoming.cancel()
                    return@coroutineScope
                }
                message as? Frame.Text ?: continue
                val json = Json.parseToJsonElement(message.readText()) as? JsonObject ?: continue
                val type = json["type"] as? JsonPrimitive ?: continue
                val handlers = messageHandlers[type.content] ?: continue
                handlers.forEach {
                    launch {
                        runCatching {
                            it.handleMessage(json)
                        }.onFailure {
                            logger.error("Error handling WebSocket message", it)
                        }
                    }
                }
            }
        }
    }
}

@Serializable
private data class ReadyBody(
    @SerialName("servers")
    val guilds: List<RevoltGuildResponse>,
    val channels: List<RevoltChannelResponse>,
)
