package com.nexamusic.air.data.repository

import com.nexamusic.air.data.model.Track
import com.nexamusic.air.network.innertube.InnerTubeClient

class MusicRepository(private val innerTubeClient: InnerTubeClient) {
    suspend fun search(query: String): List<Track> = innerTubeClient.searchSongs(query)

    suspend fun withResolvedStream(track: Track): Track {
        if (track.localUri != null || track.streamUrl != null) return track
        val stream = innerTubeClient.resolveStreamUrl(track.id)
        return track.copy(streamUrl = stream)
    }
}
