package com.nexamusic.air.data.database

import androidx.room.Entity

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "trackId"])
data class PlaylistSongEntity(
    val playlistId: Long,
    val trackId: String,
    val title: String,
    val artist: String,
    val streamUrl: String?,
    val localUri: String?
)
