package com.example.soverloadtracker.presentation.dataStorage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soverloadtracker.presentation.sensorDataGathering.BackgroundTrackingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(private val settingsManager: SettingsManager, val appContext: Context) :
    ViewModel() {

    //Bools
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
        viewModelScope.launch {
            //save setting value
            settingsManager.updateSetting(SettingsManager.BACKGROUND_TRACKING, enabled)

            //start tracking
            try {
                val hrManager = BackgroundTrackingManager(appContext)
                if (enabled) {
                    hrManager.startHRTracking()
                } else {
                    hrManager.stopHRTracking()
                }
            } catch (e: Exception) {
                e.printStackTrace()

                //reset toggle so UI stays in sync
                settingsManager.updateSetting(SettingsManager.BACKGROUND_TRACKING, false)
                //show Toast message on main thread
                withContext(Dispatchers.Main) {
                    val message = if (e is SecurityException) {
                        "Permission denied. Please allow Body Sensors in settings."
                    } else {
                        "Error: Could not start heart rate tracking."
                    }
                    android.widget.Toast.makeText(
                        appContext,
                        message,
                        android.widget.Toast.LENGTH_LONG
                    ).show()

                }
            }
        }
    }

    fun toggleAutoTriggers(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateSetting(
                SettingsManager.AUTO_TRIGGERS,
                enabled
            )
        }
    }

    fun toggleBrightLight(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateSetting(
                SettingsManager.BRIGHT_LIGHT,
                enabled
            )
        }
    }

    fun toggleStrobingLight(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateSetting(
                SettingsManager.STROBING_LIGHT,
                enabled
            )
        }
    }

    fun toggleLoudSound(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateSetting(
                SettingsManager.LOUD_SOUND,
                enabled
            )
        }
    }
}