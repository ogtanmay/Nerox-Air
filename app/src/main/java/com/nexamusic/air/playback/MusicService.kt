package com.nexamusic.air.playback

import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

@UnstableApi
class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val manager = PlaybackModule.playerManager(this)
        mediaSession = MediaSession.Builder(this, manager.player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
