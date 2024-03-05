package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.revolt.RevoltManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RevoltUser(
    override val manager: RevoltManager,
    override val id: String,
    override val name: String,
    override val effectiveName: String,
    override val effectiveAvatarUrl: String,
    override val isBot: Boolean,
    val ownerId: String? = null,
) : BaseEntity(), User {

    override val isSelf: Boolean = manager.selfId == id
    override val asMention: String = "<@$id>"
    override val asBasicMention: String = "@$effectiveName"

    override suspend fun getBannerUrl(): String? = null
}

private const val DEFAULT_AVATAR_URL: String =
    "https://autumn.revolt.chat/attachments/iHTRCGdFPvQviBkUXwX2Z0vj-0ilaCHcaCNmvBnyuc/default_avatar.png"

@Serializable
data class RevoltUserResponse(
    @SerialName("_id")
    val id: String,
    val username: String,
    @SerialName("display_name")
    val displayName: String? = null,
    val avatar: RevoltAvatarBody? = null,
    val bot: RevoltBotBody? = null,
) {

    fun convert(manager: RevoltManager): RevoltUser =
        RevoltUser(
            manager = manager,
            id = id,
            name = username,
            effectiveName = displayName ?: username,
            effectiveAvatarUrl = avatar?.getUrl(manager) ?: "$DEFAULT_AVATAR_URL/defaults/avatar.png",
            isBot = bot != null,
            ownerId = bot?.ownerId,
        )
}

@Serializable
data class RevoltAvatarBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
) {

    fun getUrl(manager: RevoltManager): String =
        "${manager.cdnDomain}/avatars/$id/$filename"
}

@Serializable
data class RevoltBotBody(
    @SerialName("owner")
    val ownerId: String,
)
