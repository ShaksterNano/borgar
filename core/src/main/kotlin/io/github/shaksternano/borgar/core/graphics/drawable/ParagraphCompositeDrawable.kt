package io.github.shaksternano.borgar.core.graphics.drawable

import io.github.shaksternano.borgar.core.media.graphics.TextAlignment
import java.awt.Graphics2D
import kotlin.math.max
import kotlin.time.Duration

private val SPACE: Drawable = TextDrawable(" ")

class ParagraphCompositeDrawable(
    private val alignment: TextAlignment,
    maxWidth: Int,
) : CompositeDrawable() {

    private val maxWidth: Int = max(0, maxWidth)

    override suspend fun draw(graphics: Graphics2D, x: Int, y: Int, timestamp: Duration) {
        val metrics = graphics.fontMetrics
        val lineHeight = metrics.ascent + metrics.descent
        val lineSpace = metrics.leading
        var lineWidth = 0
        var lineY = y

        val currentLine = mutableListOf<Drawable>()
        parts.forEachIndexed { i, part ->
            val resizedPart = part.resizeToHeight(lineHeight)?.also {
                parts[i] = it
            } ?: part
            val partWidth = resizedPart.width(graphics)
            var spaceWidth = SPACE.width(graphics)
            var newLineWidth = lineWidth + partWidth
            if (lineWidth > 0) {
                newLineWidth += spaceWidth
            }
            if (newLineWidth <= maxWidth || currentLine.isEmpty()) {
                currentLine.add(resizedPart)
                lineWidth = newLineWidth
            } else {
                var lineX = calculateTextXPosition(alignment, x, lineWidth, maxWidth)
                if (alignment == TextAlignment.JUSTIFY) {
                    spaceWidth += (maxWidth - lineWidth) / (currentLine.size - 1)
                }
                currentLine.forEach {
                    it.draw(graphics, lineX, lineY, timestamp)
                    lineX += it.width(graphics) + spaceWidth
                }
                currentLine.clear()
                currentLine.add(resizedPart)
                lineWidth = partWidth
                lineY += lineHeight + lineSpace
            }
        }

        var lineX = calculateTextXPosition(alignment, x, lineWidth, maxWidth)
        currentLine.forEach {
            it.draw(graphics, lineX, lineY, timestamp)
            lineX += it.width(graphics) + SPACE.width(graphics)
        }
    }

    private fun calculateTextXPosition(alignment: TextAlignment, x: Int, lineWidth: Int, maxWidth: Int): Int =
        when (alignment) {
            TextAlignment.CENTRE -> x + (maxWidth - lineWidth) / 2
            TextAlignment.RIGHT -> x + maxWidth - lineWidth
            else -> x
        }

    override fun width(graphicsContext: Graphics2D): Int {
        val metrics = graphicsContext.fontMetrics
        val lineHeight = metrics.ascent + metrics.descent
        var lineWidth = 0
        var maxLineWidth = 0

        var currentLineIsEmpty = true
        parts.forEach {
            val resizedPart = it.resizeToHeight(lineHeight) ?: it
            val partWidth = resizedPart.width(graphicsContext)
            val spaceWidth = SPACE.width(graphicsContext)
            var newLineWidth = lineWidth + partWidth
            if (lineWidth > 0) {
                newLineWidth += spaceWidth
            }
            if (newLineWidth <= maxWidth || currentLineIsEmpty) {
                lineWidth = newLineWidth
                currentLineIsEmpty = false
            } else {
                lineWidth = partWidth
                currentLineIsEmpty = true
            }
            maxLineWidth = max(maxLineWidth, lineWidth)
        }

        return maxLineWidth
    }

    override fun height(graphicsContext: Graphics2D): Int {
        val metrics = graphicsContext.fontMetrics
        val lineHeight = metrics.ascent + metrics.descent
        val lineSpace = metrics.leading
        var lineWidth = 0
        var lineY = 0

        var currentLineIsEmpty = true
        parts.forEach {
            val resizedPart = it.resizeToHeight(lineHeight) ?: it
            val partWidth = resizedPart.width(graphicsContext)
            val spaceWidth = SPACE.width(graphicsContext)
            var newLineWidth = lineWidth + partWidth
            if (lineWidth > 0) {
                newLineWidth += spaceWidth
            }
            if (newLineWidth <= maxWidth || currentLineIsEmpty) {
                lineWidth = newLineWidth
                currentLineIsEmpty = false
            } else {
                lineWidth = partWidth
                lineY += lineHeight + lineSpace
                currentLineIsEmpty = true
            }
        }

        lineY += lineHeight
        return lineY
    }

    override fun resizeToHeight(height: Int): Drawable? = null
}
