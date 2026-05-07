package com.example.soverloadtracker.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.soverloadtracker.R


/**
 * Page prompting the user to add factors in a log (or skip).
 * 2nd page of the app.
 * @param onSkip Callback to skip manual logging and move to end button
 * @param onAddFactors Callback to move to factors menu
 */
@Composable
fun ExtraFactorsPrompt(onSkip: () -> Unit, onAddFactors: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //skip button
        Button(
            onClick = { onSkip() },
            modifier = Modifier
                .weight(1f)
                .padding(top = 18.dp)
                .padding(bottom = 3.dp)
                .fillMaxWidth(0.9f),
            //.fillMaxHeight(0.6f),
            shape = ButtonDefaults.shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = stringResource(R.string.skip_button_text),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }

        //factors button
        FilledTonalButton(
            onClick = { onAddFactors() },
            modifier = Modifier
                .weight(0.5f)
                .padding(bottom = 8.dp)
                .fillMaxWidth(0.8f),
            //.fillMaxHeight(0.3f),
            shape = ButtonDefaults.shape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.factors_button_text))
        }
    }
}


/**
 * Menu page for adding factors, which will branch to the individual categories
 * @param onConfirm Callback to move to the next page
 * @param toCategory Callback to move to the category page
 */
@Composable
fun FactorMainMenu(
    onConfirm: () -> Unit,
    toCategory: (String) -> Unit
) {
    ScalingLazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = stringResource(R.string.factors_button_text),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        //rows for grid layout
        item {
            Row {
                FactorIconButton(
                    Icons.Default.LightMode,
                    stringResource(R.string.light_category),
                    onClick = { toCategory("light") })
                Spacer(Modifier.width(8.dp))
                FactorIconButton(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    stringResource(R.string.sound_category),
                    onClick = { toCategory("sound") })
                Spacer(Modifier.width(8.dp))
                FactorIconButton(
                    Icons.Default.Air,
                    stringResource(R.string.smell_category),
                    onClick = { toCategory("smell") })
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        item {
            Row {
                FactorIconButton(
                    Icons.Default.TouchApp,
                    stringResource(R.string.touch_category),
                    onClick = { toCategory("touch") })
                Spacer(Modifier.width(8.dp))
                FactorIconButton(
                    Icons.Default.Restaurant,
                    stringResource(R.string.taste_category),
                    onClick = { toCategory("taste") })
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        // ok Button
        item {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(stringResource(android.R.string.ok))
            }
        }
    }
}


/**
 * Button builder for the factor menu.
 * @param icon Icon to display
 * @param label Label to display
 */
@Composable
fun FactorIconButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(ButtonDefaults.SmallIconSize * 2)
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
