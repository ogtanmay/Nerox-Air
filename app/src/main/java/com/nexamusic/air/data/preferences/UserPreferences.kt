package com.nexamusic.air.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data.map { it[DARK_THEME] ?: true }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[DARK_THEME] = enabled
        }
    }
}
