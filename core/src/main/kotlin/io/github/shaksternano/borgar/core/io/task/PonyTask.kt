package io.github.shaksternano.borgar.core.io.task

import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable

private const val PONY_API_DOMAIN = "https://www.theponyapi.com"

class PonyTask(
    tags: String,
    fileCount: Int,
    maxFileSize: Long,
) : ApiFilesTask(
    tags,
    fileCount,
    filePrefix = "pony",
    maxFileSize,
) {

    override fun requestUrl(tags: Set<String>): String = buildString {
        append("$PONY_API_DOMAIN/api/v1/pony/random")
        if (tags.isNotEmpty()) {
            tags.joinTo(this, ",", "?q=") {
                it.replace(" ", "%20")
            }
        }
    }

    override suspend fun parseResponse(response: HttpResponse): ApiResponse {
        val body = response.body<ResponseBody>()
        return ApiResponse(
            body.pony.id.toString(),
            body.pony.representations.full,
            body.pony.originalFormat,
        )
    }

    @Serializable
    private data class ResponseBody(
        val pony: PonyBody,
    )

    @Serializable
    private data class PonyBody(
        val id: Long,
        val originalFormat: String,
        val representations: Representations,
    )

    @Serializable
    private data class Representations(
        val full: String,
    )
}