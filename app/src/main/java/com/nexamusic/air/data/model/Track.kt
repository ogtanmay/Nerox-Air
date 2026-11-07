package com.nexamusic.air.data.model

import android.net.Uri

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val artworkUrl: String?,
    val streamUrl: String? = null,
    val localUri: Uri? = null,
    val lyricsLrc: String? = null
)
