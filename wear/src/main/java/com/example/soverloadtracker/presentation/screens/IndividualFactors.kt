package com.example.soverloadtracker.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.example.soverloadtracker.R
import com.example.soverloadtracker.SqLiteDatabase
import com.example.soverloadtracker.presentation.dataStorage.LogData
import com.example.soverloadtracker.presentation.dataStorage.ThresholdData
import com.example.soverloadtracker.presentation.sensorDataGathering.SensorDataComputer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Expanded tactile factor logging page
 * @param currentLog Log being contstructed
 * @param onNext Callback to move back to menu page
 */
@Composable
fun LogTouchMenu(currentLog: LogData, onNext: (LogData) -> Unit) {
    // state management for toggles
    var textureSelected by remember { mutableStateOf(currentLog.tactileBad) }
    var personalSpaceSelected by remember { mutableStateOf(currentLog.tactilePersonalContact) }
    var otherSelected by remember { mutableStateOf(currentLog.tactileOther) }

    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = columnState,
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = stringResource(R.string.touch_menu_title),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            //toggles
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CheckboxButton(
                        checked = textureSelected,
                        onCheckedChange = { textureSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.texture_toggle)) }
                    )

                    CheckboxButton(
                        checked = personalSpaceSelected,
                        onCheckedChange = { personalSpaceSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.personal_space_toggle)) }
                    )

                    CheckboxButton(
                        checked = otherSelected,
                        onCheckedChange = { otherSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.other_toggle)) }
                    )
                }
            }

            //Next button
            item {
                Button(
                    onClick = {
                        currentLog.tactileBad = textureSelected
                        currentLog.tactilePersonalContact = personalSpaceSelected
                        currentLog.tactileOther = otherSelected

                        onNext(currentLog)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(40.dp),
                    shape = ButtonDefaults.shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.next_button))
                }
            }
        }

    }
}

/**
 * Expanded smell factor logging page
 * @param currentLog Log being constructed
 * @param onNext Callback to move back to menu page
 */
@Composable
fun LogSmellMenu(currentLog: LogData, onNext: (LogData) -> Unit) {
    // state management for toggles
    var strongSelected by remember { mutableStateOf(currentLog.smellStrong) }
    var otherSelected by remember { mutableStateOf(currentLog.smellOther) }

    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = columnState,
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = stringResource(R.string.smell_factors_title),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            //toggles
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CheckboxButton(
                        checked = strongSelected,
                        onCheckedChange = { strongSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.smell_strong_toggle)) }
                    )

                    CheckboxButton(
                        checked = otherSelected,
                        onCheckedChange = { otherSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.smell_other_trigger)) }
                    )
                }
            }

            //Next button
            item {
                Button(
                    onClick = {
                        currentLog.smellStrong = strongSelected
                        currentLog.smellOther = otherSelected

                        onNext(currentLog)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(40.dp),
                    shape = ButtonDefaults.shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.next_button))
                }
            }
        }

    }
}


/**
 * Expanded taste factor logging page
 * @param currentLog Log being constructed
 * @param onNext Callback to move back to menu page
 */
@Composable
fun LogTasteMenu(currentLog: LogData, onNext: (LogData) -> Unit) {
    // state management for toggles
    var strongSelected by remember { mutableStateOf(currentLog.tasteStrong) }
    var badSelected by remember { mutableStateOf(currentLog.tasteBad) }
    var otherSelected by remember { mutableStateOf(currentLog.tasteOther) }

    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = columnState,
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = stringResource(R.string.taste_factors_title),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            //toggles
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CheckboxButton(
                        checked = strongSelected,
                        onCheckedChange = { strongSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.strong_taste_toggle)) }
                    )

                    CheckboxButton(
                        checked = badSelected,
                        onCheckedChange = { badSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.unpleasant_taste_title)) }
                    )

                    CheckboxButton(
                        checked = otherSelected,
                        onCheckedChange = { otherSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.other_toggle)) }
                    )
                }
            }

            //Next button
            item {
                Button(
                    onClick = {
                        currentLog.tasteStrong = strongSelected
                        currentLog.tasteBad = badSelected
                        currentLog.tasteOther = otherSelected

                        onNext(currentLog)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(40.dp),
                    shape = ButtonDefaults.shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.next_button))
                }
            }
        }

    }
}


/**
 * Expanded light factor logging page.
 * Overrides sensor readings for manually log brightness/strobing by using extreme values
 * @param currentLog Log being constructed
 * @param onNext Callback to move back to menu page
 */
@Composable
fun LogLightMenu(currentLog: LogData, onNext: (LogData) -> Unit, database: SqLiteDatabase) {
    // state management for toggles
    var brightSelected by remember { mutableStateOf(currentLog.avgLux >= SensorDataComputer.HIGH_LIGHT_LEVEL) }
    var strobingSelected by remember { mutableStateOf(currentLog.luxStdev >= SensorDataComputer.STROBING_STDEV_THRESHOLD) }
    var otherSelected by remember { mutableStateOf(currentLog.lightOther) }

    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = columnState,
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = stringResource(R.string.light_factors_title),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            //toggles
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CheckboxButton(
                        checked = brightSelected,
                        onCheckedChange = { brightSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.bright_light_toggle)) }
                    )

                    CheckboxButton(
                        checked = strobingSelected,
                        onCheckedChange = { strobingSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.strobing_light_toggle)) }
                    )

                    CheckboxButton(
                        checked = otherSelected,
                        onCheckedChange = { otherSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.other_toggle)) }
                    )
                }
            }

            //Next button
            item {
                Button(
                    onClick = {

                        if (strobingSelected && currentLog.luxStdev <= SensorDataComputer.STROBING_STDEV_THRESHOLD) {
                            currentLog.luxStdev = 999f
                        } else if (!strobingSelected && currentLog.luxStdev > SensorDataComputer.STROBING_STDEV_THRESHOLD) {
                            currentLog.luxStdev = -1f
                        }
                        currentLog.wasBright = brightSelected
                        currentLog.lightOther = otherSelected

                        onNext(currentLog)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(40.dp),
                    shape = ButtonDefaults.shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.next_button))
                }
            }
        }

    }
}

/**
 * Expanded sound factor logging page.
 * Overrides sensor readings to manually log loudness by using extreme values
 * @param currentLog Log being constructed
 * @param onNext Callback to move back to menu page
 */
@Composable
fun LogSoundMenu(currentLog: LogData, onNext: (LogData) -> Unit, database: SqLiteDatabase) {
    // state management for toggles
    var loudSelected by remember { mutableStateOf(currentLog.avgDecibels >= SensorDataComputer.DECIBEL_THRESHOLD) }
    var otherSelected by remember { mutableStateOf(currentLog.noiseOther) }

    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = columnState,
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = stringResource(R.string.sound_factors_title),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            //toggles
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CheckboxButton(
                        checked = loudSelected,
                        onCheckedChange = { loudSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.loud_sound_toggle)) }
                    )

                    CheckboxButton(
                        checked = otherSelected,
                        onCheckedChange = { otherSelected = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.other_toggle)) }
                    )
                }
            }

            //Next button
            item {
                Button(
                    onClick = {
                        currentLog.wasLoud = loudSelected
                        currentLog.noiseOther = otherSelected

                        onNext(currentLog)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(40.dp),
                    shape = ButtonDefaults.shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.next_button))
                }
            }
        }

    }
}
