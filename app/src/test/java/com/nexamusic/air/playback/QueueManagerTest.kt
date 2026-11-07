package com.nexamusic.air.playback

import com.nexamusic.air.data.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class QueueManagerTest {
    private val tracks = listOf(
        Track("1", "A", "AA", 1_000, null),
        Track("2", "B", "BB", 1_000, null),
        Track("3", "C", "CC", 1_000, null)
    )

    @Test
    fun `next loops queue`() {
        val manager = QueueManager()
        manager.setQueue(tracks, startIndex = 2)

        val next = manager.next(shuffle = false)

        assertEquals(0, next)
    }

    @Test
    fun `shuffle avoids current index when possible`() {
        val manager = QueueManager()
        manager.setQueue(tracks, startIndex = 1)

        val next = manager.next(shuffle = true)

        assertNotEquals(1, next)
    }
}
