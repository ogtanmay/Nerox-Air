package com.nexamusic.air.playback

data class ControllerState(
    val isReady: Boolean = false,
    val isPlaying: Boolean = false,
    val currentIndex: Int = 0,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = 0
)
