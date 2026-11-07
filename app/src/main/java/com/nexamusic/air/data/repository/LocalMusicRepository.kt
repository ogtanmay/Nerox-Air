package com.nexamusic.air.data.repository

import android.content.Context
import android.provider.MediaStore
import com.nexamusic.air.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMusicRepository(private val context: Context) {
    suspend fun loadLocalTracks(): List<Track> = withContext(Dispatchers.IO) {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(uri, projection, selection, null, "${MediaStore.Audio.Media.DATE_ADDED} DESC")
            ?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                buildList {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idIndex)
                        add(
                            Track(
                                id = id.toString(),
                                title = cursor.getString(titleIndex) ?: "Unknown title",
                                artist = cursor.getString(artistIndex) ?: "Unknown artist",
                                durationMs = cursor.getLong(durationIndex),
                                artworkUrl = null,
                                localUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon()
                                    .appendPath(id.toString())
                                    .build()
                            )
                        )
                    }
                }
            }
            ?: emptyList()
    }
}
