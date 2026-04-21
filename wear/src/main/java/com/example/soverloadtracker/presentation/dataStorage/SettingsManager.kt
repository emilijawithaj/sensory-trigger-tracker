package com.example.soverloadtracker.presentation.dataStorage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val BACKGROUND_TRACKING = booleanPreferencesKey("background_tracking")
        val AUTO_TRIGGERS = booleanPreferencesKey("auto_triggers")
        val BRIGHT_LIGHT = booleanPreferencesKey("bright_light")
        val STROBING_LIGHT = booleanPreferencesKey("strobing_light")
        val LOUD_SOUND = booleanPreferencesKey("loud_sound")
    }

    //read settings using Flow, mapping the settings in the dataStore
    val backgroundTrackingFlow: Flow<Boolean> = context.dataStore.data.map { it[BACKGROUND_TRACKING] ?: false }
    val autoTriggersFlow: Flow<Boolean> = context.dataStore.data.map { it[AUTO_TRIGGERS] ?: false }
    val brightLightFlow: Flow<Boolean> = context.dataStore.data.map { it[BRIGHT_LIGHT] ?: false }
    val strobingLightFlow: Flow<Boolean> = context.dataStore.data.map { it[STROBING_LIGHT] ?: false }
    val loudSoundFlow: Flow<Boolean> = context.dataStore.data.map { it[LOUD_SOUND] ?: false }

    //save settings
    suspend fun updateSetting(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }
}