package io.github.shaksternano.borgar.core.io

import com.google.common.io.Closer
import com.google.common.io.Files
import io.github.shaksternano.borgar.core.media.mediaFormat
import io.github.shaksternano.borgar.core.util.JSON
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.readByteArray
import org.apache.commons.io.FileUtils
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.regex.Pattern
import kotlin.io.path.*
import kotlin.random.Random
import kotlin.random.nextULong

private object IOUtil

private val TEMP_DIR: Path = Path(System.getProperty("java.io.tmpdir"))

val ALLOWED_DOMAINS: Set<String> = setOf(
    "raw.githubusercontent.com",
    "cdn.discordapp.com",
    "media.discordapp.net",
    "autumn.revolt.chat",
    "pbs.twimg.com",
    "i.redd.it",
)

suspend fun createTemporaryFile(filename: String): Path = createTemporaryFile(
    filenameWithoutExtension(filename),
    fileExtension(filename),
)

suspend fun createTemporaryFile(filenameWithoutExtension: String, extension: String): Path {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    val path = withContext(Dispatchers.IO) {
        createTempFile(filenameWithoutExtension, extensionWithDot)
    }
    path.toFile().deleteOnExit()
    return path
}

suspend fun getTemporaryFile(filename: String): Path = getTemporaryFile(
    filenameWithoutExtension(filename),
    fileExtension(filename),
)

suspend fun getTemporaryFile(filenameWithoutExtension: String, extension: String): Path {
    val extension1 = extension.ifBlank { "tmp" }
    var filename = ""
    var path = TEMP_DIR.resolve(filename)
    while (filename.isBlank() || withContext(Dispatchers.IO) { path.exists() }) {
        filename = filenameWithoutExtension + Random.nextULong() + "." + extension1
        path = TEMP_DIR.resolve(filename)
    }
    path.toFile().deleteOnExit()
    return path
}

suspend fun getResource(resourcePath: String): InputStream =
    withContext(Dispatchers.IO) {
        IOUtil.javaClass.classLoader.getResourceAsStream(resourcePath)
    } ?: throw FileNotFoundException("Resource not found: $resourcePath")

suspend fun forEachResource(
    directory: String,
    operation: suspend (resourcePath: String, inputStream: InputStream) -> Unit
) {
    // Remove trailing forward slashes
    val trimmedDirectory = directory.trim { it <= ' ' }.replace("/$".toRegex(), "")
    val packageName = trimmedDirectory.replace(Pattern.quote("/").toRegex(), ".")
    getResourcePaths(packageName).forEach { resourcePath ->
        getResource(resourcePath).use { inputStream ->
            operation(resourcePath, inputStream)
        }
    }
}

private suspend fun getResourcePaths(packageName: String): Set<String> = withContext(Dispatchers.IO) {
    val reflections = Reflections(packageName, Scanners.Resources)
    reflections.getResources("(.*?)")
}

val Path.filename: String
    get() = fileName?.toString() ?: throw IllegalArgumentException("Invalid path")

suspend fun Path.deleteSilently() {
    runCatching {
        withContext(Dispatchers.IO) {
            @OptIn(ExperimentalPathApi::class)
            deleteRecursively()
        }
    }
}

fun httpClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient = HttpClient(CIO, block)

fun configuredHttpClient(json: Boolean = true): HttpClient = httpClient {
    if (json) {
        install(ContentNegotiation) {
            json(JSON)
        }
    }
    install(HttpRequestRetry) {
        maxRetries = 3
        retryIf { _, response ->
            response.status == HttpStatusCode.TooManyRequests
        }
        constantDelay(5000)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 60000
    }
}

inline fun <T> useHttpClient(json: Boolean = true, block: (HttpClient) -> T): T =
    configuredHttpClient(json).use(block)

inline fun <T> HttpResponse.ifSuccessful(block: (HttpResponse) -> T): T =
    if (status.isSuccess()) {
        block(this)
    } else {
        throw IOException("HTTP request failed: $status")
    }

suspend inline fun <reified T> httpGet(url: String): T = useHttpClient { client ->
    client.get(url).ifSuccessful {
        it.body<T>()
    }
}

suspend fun download(url: String, path: Path) = useHttpClient { client ->
    client.get(url).ifSuccessful {
        it.download(path)
    }
}

suspend fun HttpResponse.download(path: Path) {
    var created = false
    readBytes {
        withContext(Dispatchers.IO) {
            if (created) {
                path.writeBytes(
                    it,
                    StandardOpenOption.APPEND,
                )
            } else {
                path.writeBytes(
                    it,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE,
                )
                created = true
            }
        }
    }
}

fun HttpResponse.filename(): String? = headers["Content-Disposition"]?.let {
    val headerParts = it.split("filename=", limit = 2)
    if (headerParts.size == 2) {
        val filename = headerParts[1].trim()
            .removeSurrounding("\"")
        filename.ifBlank { null }
    } else null
}

private suspend inline fun HttpResponse.readBytes(block: (ByteArray) -> Unit) {
    val channel = bodyAsChannel()
    while (!channel.isClosedForRead) {
        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
        while (!packet.exhausted()) {
            val bytes = packet.readByteArray()
            block(bytes)
        }
    }
}

suspend fun Path.write(inputStream: InputStream) = withContext(Dispatchers.IO) {
    FileUtils.copyInputStreamToFile(inputStream, toFile())
}

suspend fun Path.inputStreamSuspend(): InputStream = withContext(Dispatchers.IO) {
    inputStream()
}

suspend fun DataSource.fileFormat(): String {
    val mediaFormat = runCatching {
        path?.let { mediaFormat(it) } ?: mediaFormat(newStream())
    }.getOrNull()
    return mediaFormat ?: fileExtension
}

suspend fun DataSource.toChannelProvider(): ChannelProvider =
    ChannelProvider(size()) {
        val url = url
        if (path == null && url != null) {
            HttpByteReadChannel(url)
        } else {
            LazyInitByteReadChannel {
                newStream().toByteReadChannel()
            }
        }
    }

fun removeQueryParams(url: String): String =
    url.split('?').first()

fun filename(filePath: String): String {
    val nameWithoutExtension = filenameWithoutExtension(filePath)
    val extension = fileExtension(filePath)
    return filename(nameWithoutExtension, extension)
}

fun filename(nameWithoutExtension: String, extension: String): String {
    val extensionWithDot = if (extension.isBlank()) "" else ".$extension"
    return nameWithoutExtension + extensionWithDot
}

fun filenameWithoutExtension(fileName: String): String =
    Files.getNameWithoutExtension(removeQueryParams(fileName))

fun fileExtension(fileName: String): String =
    Files.getFileExtension(removeQueryParams(fileName))

val DataSource.filenameWithoutExtension: String
    get() = filenameWithoutExtension(filename)

val DataSource.fileExtension: String
    get() = fileExtension(filename)

fun toMb(bytes: Long): Long =
    bytes / 1024 / 1024

fun closeAll(vararg closeables: Closeable?) =
    closeAll(closeables.asIterable())

fun closeAll(closeables: Iterable<Closeable?>) =
    Closer.create().use {
        for (closeable in closeables) {
            it.register(closeable)
        }
    }

suspend fun InputStream.readSuspend(): Int = withContext(Dispatchers.IO) {
    read()
}

suspend fun InputStream.readNBytesSuspend(n: Int): ByteArray = withContext(Dispatchers.IO) {
    readNBytes(n)
}

suspend fun InputStream.skipNBytesSuspend(n: Long) = withContext(Dispatchers.IO) {
    skipNBytes(n)
}

suspend inline fun <R> DataSource.useFile(block: (FileDataSource) -> R): R {
    val isInputTemp = path == null
    val fileInput = getOrWriteFile()
    return try {
        block(fileInput)
    } finally {
        if (isInputTemp) {
            fileInput.path.deleteSilently()
        }
    }
}
