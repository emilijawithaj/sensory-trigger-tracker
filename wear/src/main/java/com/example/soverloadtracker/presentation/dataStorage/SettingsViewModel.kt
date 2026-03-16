package com.example.soverloadtracker.presentation.dataStorage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {

    // Bools
    val isBackgroundTrackingEnabled = settingsManager.backgroundTrackingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAutoTriggersEnabled = settingsManager.autoTriggersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBrightLightEnabled = settingsManager.brightLightFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isStrobingLightEnabled = settingsManager.strobingLightFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isLoudSoundEnabled = settingsManager.loudSoundFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    //toggle handling
    fun toggleBackgroundTracking(enabled: Boolean) {
        viewModelScope.launch { settingsManager.updateSetting(SettingsManager.BACKGROUND_TRACKING, enabled) }
    }

    fun toggleAutoTriggers(enabled: Boolean) {
        viewModelScope.launch { settingsManager.updateSetting(SettingsManager.AUTO_TRIGGERS, enabled) }
    }

    fun toggleBrightLight(enabled: Boolean) {
        viewModelScope.launch { settingsManager.updateSetting(SettingsManager.BRIGHT_LIGHT, enabled) }
    }

    fun toggleStrobingLight(enabled: Boolean) {
        viewModelScope.launch { settingsManager.updateSetting(SettingsManager.STROBING_LIGHT, enabled) }
    }

    fun toggleLoudSound(enabled: Boolean) {
        viewModelScope.launch { settingsManager.updateSetting(SettingsManager.LOUD_SOUND, enabled) }
    }
}