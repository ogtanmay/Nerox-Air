package com.nexamusic.air.ui.lyrics

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

object LrcParser {
    private val regex = Regex("\\[(\\d{1,2}):(\\d{1,2})(?:\\.(\\d{1,3}))?]\\s*(.*)")

    fun parse(lrc: String): List<LyricLine> {
        return lrc.lineSequence()
            .mapNotNull { line ->
                val match = regex.find(line) ?: return@mapNotNull null
                val minutes = match.groupValues[1].toLongOrNull() ?: return@mapNotNull null
                val seconds = match.groupValues[2].toLongOrNull() ?: return@mapNotNull null
                val fractionRaw = match.groupValues[3]
                val fraction = when (fractionRaw.length) {
                    0 -> 0L
                    1 -> fractionRaw.toLong() * 100
                    2 -> fractionRaw.toLong() * 10
                    else -> fractionRaw.take(3).toLong()
                }
                LyricLine(
                    timestampMs = minutes * 60_000 + seconds * 1_000 + fraction,
                    text = match.groupValues[4]
                )
            }
            .sortedBy { it.timestampMs }
            .toList()
    }

    fun activeIndex(lines: List<LyricLine>, positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        val index = lines.indexOfLast { it.timestampMs <= positionMs }
        return index.coerceAtLeast(0)
    }
}
