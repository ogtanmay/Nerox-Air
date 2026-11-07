package com.nexamusic.air.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavDestination(
    val label: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Rounded.Home),
    Search("Search", Icons.Rounded.Search),
    Library("Library", Icons.Rounded.LibraryMusic),
    Settings("Settings", Icons.Rounded.Settings)
}
