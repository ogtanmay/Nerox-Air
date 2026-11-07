package com.nexamusic.air.ui.lyrics

import org.junit.Assert.assertEquals
import org.junit.Test

class LrcParserTest {
    @Test
    fun `parse reads and sorts timestamps`() {
        val lines = LrcParser.parse(
            """
            [00:10.00]second
            [00:01.50]first
            """.trimIndent()
        )

        assertEquals(2, lines.size)
        assertEquals("first", lines[0].text)
        assertEquals(1_500L, lines[0].timestampMs)
    }

    @Test
    fun `active index returns nearest previous line`() {
        val lines = LrcParser.parse("[00:01.00]a\n[00:05.00]b")
        assertEquals(0, LrcParser.activeIndex(lines, 2_000L))
        assertEquals(1, LrcParser.activeIndex(lines, 6_000L))
    }
}
