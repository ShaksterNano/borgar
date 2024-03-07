package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.revolt.RevoltManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RevoltCustomEmoji(
    override val manager: RevoltManager,
    override val id: String,
    override val name: String,
    override val imageUrl: String,
) : CustomEmoji, BaseEntity() {

    override val asMention: String = manager.emojiAsTyped(id)
    override val asBasicMention: String = manager.emojiAsTyped(id)
}

@Serializable
data class RevoltEmojiResponse(
    @SerialName("_id")
    val id: String,
    val name: String,
    val animated: Boolean = false,
) {
    fun convert(manager: RevoltManager) = RevoltCustomEmoji(
        manager = manager,
        id = id,
        name = name,
        imageUrl = "${manager.cdnDomain}/emojis/$id/$name.${if (animated) "gif" else "png"}"
    )
}
