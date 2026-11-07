package com.nexamusic.air.playback

import com.nexamusic.air.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class QueueManager {
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        _queue.value = tracks
        _currentIndex.value = startIndex.coerceIn(0, (tracks.lastIndex).coerceAtLeast(0))
    }

    fun current(): Track? = _queue.value.getOrNull(_currentIndex.value)

    fun next(shuffle: Boolean = false): Int {
        val list = _queue.value
        if (list.isEmpty()) return 0
        _currentIndex.value = if (shuffle && list.size > 1) {
            var next: Int
            do {
                next = Random.nextInt(list.size)
            } while (next == _currentIndex.value)
            next
        } else {
            (_currentIndex.value + 1).mod(list.size)
        }
        return _currentIndex.value
    }

    fun previous(): Int {
        val list = _queue.value
        if (list.isEmpty()) return 0
        _currentIndex.value = (_currentIndex.value - 1).mod(list.size)
        return _currentIndex.value
    }
}
