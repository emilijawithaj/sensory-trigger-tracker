package com.example.soverloadtracker.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.example.soverloadtracker.R

@Composable
fun SettingsPage() {
    // In a real app, collect this state from a ViewModel/DataStore
    var isBackgroundTrackingEnabled by remember { mutableStateOf(false) }
    var isAutoTriggersEnabled by remember { mutableStateOf(false) }

    var brightLightTracking by remember { mutableStateOf(false) }
    var strobingLightTracking by remember { mutableStateOf(false) }
    var loudSoundTracking by remember { mutableStateOf(false) }

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
                onCheckedChange = { isBackgroundTrackingEnabled = it },
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
                onCheckedChange = { isAutoTriggersEnabled = it },
                label = {
                    Text(stringResource(R.string.automatic_factor_tracking))
                },
                secondaryLabel = {
                    Text(
                        stringResource(R.string.automatic_factors_description)
                    )
                },
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
                checked = brightLightTracking,
                onCheckedChange = { brightLightTracking = it },
                label = {
                    Text(stringResource(R.string.bright_lights))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SwitchButton(
                checked = strobingLightTracking,
                onCheckedChange = { strobingLightTracking = it },
                label = {
                    Text(stringResource(R.string.strobing_lights))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SwitchButton(
                checked = loudSoundTracking,
                onCheckedChange = { loudSoundTracking = it },
                label = {
                    Text(stringResource(R.string.loud_background_noise))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}