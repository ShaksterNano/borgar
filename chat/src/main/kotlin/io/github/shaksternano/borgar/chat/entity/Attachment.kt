package io.github.shaksternano.borgar.chat.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.DataSourceConvertable
import io.github.shaksternano.borgar.core.io.UrlDataSource

data class Attachment(
    override val id: String,
    val url: String,
    val proxyUrl: String,
    val fileName: String,
    override val manager: BotManager,
) : BaseEntity(), DataSourceConvertable {

    override fun asDataSource(): UrlDataSource = DataSource.fromUrl(url, fileName)
}
