package com.example.soverloadtracker.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.soverloadtracker.R


/**
 * Main page of the app; the button to initiate a log
 * @param logButtonOnClick Callback to initiate a log
 */
@Composable
fun LogButton(logButtonOnClick: () -> Unit, onSettingsClick: () -> Unit) {
    Box() {
        //LOG button
        Button(
            onClick = { logButtonOnClick() },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text(
                stringResource(R.string.log_button),
                textAlign = TextAlign.Center
            )
        }

        //Settings button
        Button(
            onClick = { onSettingsClick() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .fillMaxWidth(0.6f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings_button),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.settings_button),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}