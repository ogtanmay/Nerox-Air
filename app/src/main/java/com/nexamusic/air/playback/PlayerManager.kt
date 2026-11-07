package com.nexamusic.air.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.nexamusic.air.data.model.Track

@UnstableApi
class PlayerManager(context: Context) {
    val queueManager = QueueManager()
    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        repeatMode = Player.REPEAT_MODE_OFF
    }

    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        queueManager.setQueue(tracks, startIndex)
        val items = tracks.map { track ->
            when {
                track.localUri != null -> MediaItem.fromUri(track.localUri!!)
                !track.streamUrl.isNullOrBlank() -> MediaItem.fromUri(track.streamUrl!!)
                else -> MediaItem.Builder().setMediaId(track.id).build()
            }
        }
        player.setMediaItems(items, startIndex, 0L)
        player.prepare()
    }

    fun release() {
        player.release()
    }
}
