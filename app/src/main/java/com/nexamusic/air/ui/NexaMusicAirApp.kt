package com.nexamusic.air.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.nexamusic.air.data.model.Track
import com.nexamusic.air.navigation.NavDestination
import com.nexamusic.air.ui.lyrics.LrcParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexaMusicAirApp(viewModel: MainViewModel) {
    var destination by remember { mutableStateOf(NavDestination.Home) }
    var showFullPlayer by remember { mutableStateOf(false) }

    val localTracks by viewModel.localTracks.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val controllerState by viewModel.controllerState.collectAsState()
    val currentTrack = viewModel.currentTrack()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                if (currentTrack != null) {
                    MiniPlayer(
                        track = currentTrack,
                        isPlaying = controllerState.isPlaying,
                        onTap = { showFullPlayer = true },
                        onPlayPause = viewModel::togglePlayPause,
                        onNext = viewModel::skipNext,
                        onPrevious = viewModel::skipPrevious
                    )
                }
                NavigationBar {
                    NavDestination.entries.forEach { entry ->
                        NavigationBarItem(
                            selected = destination == entry,
                            onClick = { destination = entry },
                            icon = { Icon(entry.icon, contentDescription = entry.label) },
                            label = { Text(entry.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ) {
            when (destination) {
                NavDestination.Home -> TrackListScreen(
                    title = "Recently Added",
                    tracks = localTracks,
                    onTrackClick = { index -> viewModel.playQueue(localTracks, index) }
                )

                NavDestination.Search -> SearchScreen(
                    results = searchResults,
                    onSearch = viewModel::search,
                    onTrackClick = { index -> viewModel.playQueue(searchResults, index) }
                )

                NavDestination.Library -> QueueScreen(
                    queue = queue,
                    onTrackClick = { index -> viewModel.playQueue(queue, index) }
                )

                NavDestination.Settings -> SettingsScreen()
            }
        }
    }

    if (showFullPlayer && currentTrack != null) {
        FullPlayer(
            track = currentTrack,
            positionMs = controllerState.positionMs,
            durationMs = controllerState.durationMs,
            isPlaying = controllerState.isPlaying,
            shuffle = controllerState.shuffle,
            repeatMode = controllerState.repeatMode,
            onDismiss = { showFullPlayer = false },
            onPlayPause = viewModel::togglePlayPause,
            onSeek = viewModel::seekTo,
            onNext = viewModel::skipNext,
            onPrevious = viewModel::skipPrevious,
            onShuffle = viewModel::toggleShuffle,
            onRepeat = viewModel::cycleRepeat
        )
    }
}

@Composable
private fun SearchScreen(
    results: List<Track>,
    onSearch: (String) -> Unit,
    onTrackClick: (Int) -> Unit
) {
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onSearch(it)
            },
            label = { Text("Search YouTube Music") },
            modifier = Modifier.fillMaxWidth()
        )

        TrackList(
            tracks = results,
            onTrackClick = onTrackClick,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun TrackListScreen(
    title: String,
    tracks: List<Track>,
    onTrackClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        TrackList(tracks = tracks, onTrackClick = onTrackClick, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun TrackList(
    tracks: List<Track>,
    onTrackClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tracks.isEmpty()) {
        Text("No music found", modifier = modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrackClick(index) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Rounded.QueueMusic, contentDescription = null)
                Column(modifier = Modifier.weight(1f)) {
                    Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        track.artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueScreen(queue: List<Track>, onTrackClick: (Int) -> Unit) {
    TrackListScreen(title = "Queue", tracks = queue, onTrackClick = onTrackClick)
}

@Composable
private fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("NexaMusic Air", style = MaterialTheme.typography.titleLarge)
        Text("Lightweight edition focused on fast startup and stable playback.")
    }
}

@Composable
private fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    onTap: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
            .clickable { onTap() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onPrevious) { Icon(Icons.Rounded.RepeatOne, null) }
        IconButton(onClick = onPlayPause) {
            Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null)
        }
        IconButton(onClick = onNext) { Icon(Icons.Rounded.Repeat, null) }
    }
}

@Composable
private fun FullPlayer(
    track: Track,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    shuffle: Boolean,
    repeatMode: Int,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit
) {
    val lines = remember(track.lyricsLrc) { track.lyricsLrc?.let(LrcParser::parse).orEmpty() }
    val activeLyric = remember(lines, positionMs) {
        lines.getOrNull(LrcParser.activeIndex(lines, positionMs))?.text.orEmpty()
    }

    Surface(
        modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(track.title, style = MaterialTheme.typography.headlineMedium)
            Text(track.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = positionMs.coerceAtLeast(0L).toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat()
            )
            Text(activeLyric.ifBlank { "Lyrics unavailable" }, style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onShuffle) {
                    Icon(Icons.Rounded.Shuffle, null, tint = if (shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onPrevious) { Icon(Icons.Rounded.RepeatOne, null) }
                IconButton(onClick = onPlayPause) {
                    Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null)
                }
                IconButton(onClick = onNext) { Icon(Icons.Rounded.Repeat, null) }
                IconButton(onClick = onRepeat) {
                    val icon = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                        else -> Icons.Rounded.Repeat
                    }
                    Icon(icon, null, tint = if (repeatMode == Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
