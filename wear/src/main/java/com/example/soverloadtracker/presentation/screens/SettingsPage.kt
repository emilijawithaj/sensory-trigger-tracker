package com.example.soverloadtracker.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.example.soverloadtracker.R
import com.example.soverloadtracker.presentation.PhoneListenerService
import com.example.soverloadtracker.presentation.dataStorage.SettingsViewModel

@Composable
fun SettingsPage(viewModel: SettingsViewModel) {
    // collect states from ViewModel
    val isBackgroundTrackingEnabled by viewModel.isBackgroundTrackingEnabled.collectAsState()
    val isAutoTriggersEnabled by viewModel.isAutoTriggersEnabled.collectAsState()
    val isBrightLightEnabled by viewModel.isBrightLightEnabled.collectAsState()
    val isStrobingLightEnabled by viewModel.isStrobingLightEnabled.collectAsState()
    val isLoudSoundEnabled by viewModel.isLoudSoundEnabled.collectAsState()


    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        //enable tracking toggle
        item {
            SwitchButton(
                checked = isBackgroundTrackingEnabled,
                onCheckedChange = { viewModel.toggleBackgroundTracking(it) },
                label = {
                    Text(stringResource(R.string.background_tracking_setting))
                },
                secondaryLabel = {
                    Text(stringResource(R.string.background_tracking_explanation))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        //auto select toggle
        item {
            SwitchButton(
                checked = isAutoTriggersEnabled,
                onCheckedChange = {
                    viewModel.toggleAutoTriggers(it)
                    PhoneListenerService.updatePhoneOnAutoTracker(viewModel.appContext, it)
                },
                label = {
                    Text(stringResource(R.string.automatic_factor_tracking))
                },
                secondaryLabel = {
                    Text(
                        stringResource(R.string.automatic_factors_description)
                    )
                },
                enabled = isBackgroundTrackingEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }

        //Manual toggles
        item {
            Text(
                text = stringResource(R.string.manual_tracking_selection),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        item {
            SwitchButton(
                checked = isBrightLightEnabled,
                onCheckedChange = { viewModel.toggleBrightLight(it) },
                label = {
                    Text(stringResource(R.string.bright_lights))
                },
                enabled = !isAutoTriggersEnabled && isBackgroundTrackingEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SwitchButton(
                checked = isStrobingLightEnabled,
                onCheckedChange = { viewModel.toggleStrobingLight(it) },
                label = {
                    Text(stringResource(R.string.strobing_lights))
                },
                enabled = !isAutoTriggersEnabled && isBackgroundTrackingEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SwitchButton(
                checked = isLoudSoundEnabled,
                onCheckedChange = { viewModel.toggleLoudSound(it) },
                label = {
                    Text(stringResource(R.string.loud_background_noise))
                },
                enabled = !isAutoTriggersEnabled && isBackgroundTrackingEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}