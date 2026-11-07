package com.nexamusic.air.playback

import android.content.Context
import androidx.media3.common.util.UnstableApi

@UnstableApi
object PlaybackModule {
    @Volatile
    private var holder: PlayerManager? = null

    fun playerManager(context: Context): PlayerManager {
        return holder ?: synchronized(this) {
            holder ?: PlayerManager(context.applicationContext).also { holder = it }
        }
    }
}
