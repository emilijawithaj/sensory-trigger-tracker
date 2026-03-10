package com.example.soverloadtracker.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.example.soverloadtracker.R
import com.example.soverloadtracker.presentation.dataStorage.SqLiteDatabase
import com.example.soverloadtracker.presentation.dataStorage.LogData
import kotlinx.coroutines.delay
import java.time.Instant

/**
 * Navigation graph for the app.
 * @param navController Navigation controller
 * @param activeLog Log being constructed
 * @param database Database to save logs to
 * @param goToPermissions Callback to request permissions
 * @param createLog Callback to function to create a new log
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    activeLog: LogData?,
    database: SqLiteDatabase,
    goToPermissions: () -> Unit,
    createLog: (Instant) -> LogData,
) {
    var activeLog by remember { mutableStateOf(activeLog) }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Destinations.LOG_BUTTON
    ) {
        // Main Button Page
        composable(Destinations.LOG_BUTTON) {
            LogButton(
                logButtonOnClick = {
                    goToPermissions()
                    navController.navigate(Destinations.LOADING)
                }
            )
        }

        // Loading Sensor Data
        composable(Destinations.LOADING) {
            LaunchedEffect(Unit) {
                delay(5100)
                activeLog = createLog(Instant.now())
                navController.navigate(Destinations.EXTRA_FACTORS)
            }
            LaunchedEffect(Unit) {
                delay(5100) // 5 seconds for sensor readings + small buffer
                activeLog = createLog(Instant.now())
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.loading_text),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Skip or Add Factors
        composable(Destinations.EXTRA_FACTORS) {
            ExtraFactorsPrompt(
                onSkip = {
                    activeLog?.let { database.addLogRecord(it) }
                    navController.navigate(Destinations.END_BUTTON) {
                        popUpTo(Destinations.END_BUTTON) { inclusive = true }
                    }
                },
                onAddFactors = { navController.navigate(Destinations.FACTOR_MENU) }
            )
        }

        // Factor menu
        composable(Destinations.FACTOR_MENU) {
            FactorMainMenu(
                onConfirm = { navController.navigate(Destinations.TAGS_MENU) },
                toCategory = { category -> navController.navigate(Destinations.categoryPath(category)) }
            )
        }

        // Individual factor menus
        composable(Destinations.CATEGORY_ROUTE) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")

            when (type) {
                "light" -> LogLightMenu(activeLog!!) { log ->
                    activeLog = log; navController.popBackStack()
                }

                "sound" -> LogSoundMenu(activeLog!!) { log ->
                    activeLog = log; navController.popBackStack()
                }

                "touch" -> LogTouchMenu(activeLog!!) { log ->
                    activeLog = log; navController.popBackStack()
                }

                "smell" -> LogSmellMenu(activeLog!!) { log ->
                    activeLog = log; navController.popBackStack()
                }

                "taste" -> LogTasteMenu(activeLog!!) { log ->
                    activeLog = log; navController.popBackStack()
                }
            }
        }

        // Tag menu
        composable(Destinations.TAGS_MENU) {
            AddTagsPage(activeLog!!) { log ->
                activeLog = log
                activeLog?.let {
                    database.addLogRecord(it)
                    navController.navigate(Destinations.END_BUTTON) {
                        popUpTo(Destinations.END_BUTTON) { inclusive = true }
                    }
                }
            }
        }

        //End button
        composable(Destinations.END_BUTTON) {
            EndButton {
                navController.navigate(Destinations.LOG_BUTTON) {
                    popUpTo(Destinations.LOG_BUTTON) { inclusive = true }
                }
            }
        }
    }
}


object Destinations {
    const val LOG_BUTTON = "logButton"
    const val LOADING = "loading"
    const val EXTRA_FACTORS = "extraFactors"
    const val FACTOR_MENU = "factorMenu"
    const val TAGS_MENU = "tagsMenu"
    const val END_BUTTON = "endButton"
    const val CATEGORY_ROUTE = "category/{type}"
    fun categoryPath(type: String) = "category/$type"
}