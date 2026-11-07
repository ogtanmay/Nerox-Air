package com.nexamusic.air.ui

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.core.content.ContextCompat
import com.nexamusic.air.data.model.Track
import com.nexamusic.air.data.repository.LocalMusicRepository
import com.nexamusic.air.data.repository.MusicRepository
import com.nexamusic.air.network.innertube.InnerTubeClient
import com.nexamusic.air.playback.ControllerState
import com.nexamusic.air.playback.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val localMusicRepository = LocalMusicRepository(application)
    private val musicRepository = MusicRepository(InnerTubeClient())

    private val _localTracks = MutableStateFlow<List<Track>>(emptyList())
    val localTracks: StateFlow<List<Track>> = _localTracks.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _controllerState = MutableStateFlow(ControllerState())
    val controllerState: StateFlow<ControllerState> = _controllerState.asStateFlow()

    private var controller: MediaController? = null
    private var tickerJob: Job? = null

    init {
        connectController()
        loadLocalTracks()
    }

    private fun connectController() {
        val token = SessionToken(getApplication(), ComponentName(getApplication(), MusicService::class.java))
        val future = MediaController.Builder(getApplication(), token).buildAsync()
        future.addListener({
            controller = future.get().also { mediaController ->
                mediaController.addListener(playerListener)
                _controllerState.value = _controllerState.value.copy(isReady = true)
                startTicker()
            }
        }, ContextCompat.getMainExecutor(getApplication()))
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            refreshControllerSnapshot(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            refreshControllerSnapshot()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            refreshControllerSnapshot(repeatMode = repeatMode)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            refreshControllerSnapshot(shuffle = shuffleModeEnabled)
        }
    }

    private fun refreshControllerSnapshot(
        isPlaying: Boolean? = null,
        repeatMode: Int? = null,
        shuffle: Boolean? = null
    ) {
        val c = controller ?: return
        _controllerState.value = _controllerState.value.copy(
            isPlaying = isPlaying ?: c.isPlaying,
            currentIndex = c.currentMediaItemIndex.coerceAtLeast(0),
            positionMs = c.currentPosition,
            durationMs = c.duration.takeIf { it > 0 } ?: 0L,
            repeatMode = repeatMode ?: c.repeatMode,
            shuffle = shuffle ?: c.shuffleModeEnabled
        )
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                refreshControllerSnapshot()
                delay(500)
            }
        }
    }

    fun loadLocalTracks() {
        viewModelScope.launch(Dispatchers.IO) {
            _localTracks.value = localMusicRepository.loadLocalTracks()
        }
    }

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchResults.value = if (query.isBlank()) emptyList() else musicRepository.search(query)
        }
    }

    fun playQueue(tracks: List<Track>, startIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolved = tracks.map { track ->
                if (track.localUri != null) track
                else musicRepository.withResolvedStream(track)
            }.filter { it.localUri != null || !it.streamUrl.isNullOrBlank() }
            _queue.value = resolved

            val c = controller ?: return@launch
            val mediaItems = resolved.map { track ->
                when {
                    track.localUri != null -> MediaItem.fromUri(track.localUri!!)
                    else -> MediaItem.fromUri(track.streamUrl!!)
                }
            }
            if (mediaItems.isNotEmpty()) {
                val safeIndex = startIndex.coerceIn(0, mediaItems.lastIndex)
                c.setMediaItems(mediaItems, safeIndex, 0L)
                c.prepare()
                c.playWhenReady = true
            }
        }
    }

    fun togglePlayPause() {
        controller?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(progressMs: Long) {
        controller?.seekTo(progressMs)
    }

    fun skipNext() {
        controller?.seekToNextMediaItem()
    }

    fun skipPrevious() {
        controller?.seekToPreviousMediaItem()
    }

    fun toggleShuffle() {
        controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun cycleRepeat() {
        controller?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun currentTrack(): Track? = _queue.value.getOrNull(_controllerState.value.currentIndex)

    override fun onCleared() {
        tickerJob?.cancel()
        controller?.release()
        controller = null
        super.onCleared()
    }
}
