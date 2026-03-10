/* App logging various environmental factors on button press to track trends in logging environments
 */

package com.example.soverloadtracker.presentation

import android.Manifest
import android.R.style
import android.content.pm.PackageManager
import android.health.connect.HealthPermissions
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.soverloadtracker.R
import com.example.soverloadtracker.SqLiteDatabase
import com.example.soverloadtracker.presentation.screens.AppNavigation
import com.example.soverloadtracker.presentation.theme.AppTheme
import kotlinx.coroutines.delay
import java.time.Instant


class MainActivity : ComponentActivity() {
    private val sensorReadManager by lazy {
        SensorReader(
            context = this,
            coroutineScope = lifecycleScope
        )
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(style.Theme_DeviceDefault)
        //val userDB = SqLiteDatabase.getInstance(this)
        //userDB.onUpgrade(userDB.writableDatabase, 1, 1)

        setContent {
            AppTheme {
                WearApp({
                    checkPermissions(
                        arrayListOf(
                            HealthPermissions.READ_HEART_RATE,
                            Manifest.permission.RECORD_AUDIO
                        )
                    )
                }, { createLog(Instant.now()) })
            }
        }
    }

    /*
            FUNCS
     */

    /**
     * permission launcher to enable getting readings
     */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("SOBOOT", "PERMISSION GRANTED START.")
        } else {
            Log.e("SOBOOT", "NO PERMISSION too bad")
        }
    }

    fun checkPermissions(permission: ArrayList<String>) {
        for (p in permission) {
            when {
                //Already granted, launch reading
                ContextCompat.checkSelfPermission(
                    this,
                    p
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("SOBOOT", "Permission is already granted.")
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, p
                ) -> {
                    Log.d("SOBOOT", "In rationale.")
                    permissionLauncher.launch(p)
                }

                //Ask
                else -> {
                    Log.d("SOBOOT", "Requesting permission.")
                    permissionLauncher.launch(p)
                }
            }
        }
        sensorReadManager.startHeartRateStreaming()
        sensorReadManager.takeLightReading()
        sensorReadManager.takeSoundReading()
    }

    /**
     * Create a new log
     * @param datetime time to log as Instant
     */
    fun createLog(datetime: Instant): LogData {
        val avgLux = sensorReadManager.lightReads.average().toFloat()
        val luxStdev = sensorReadManager.dataProcessor.lightStdev
        val lightOther = false //
        val avgDecibels = sensorReadManager.soundReadings.average().toFloat()
        val noiseOther = false //
        val smellStrong = false //
        val smellOther = false //
        val tactileBad = false //
        val tactilePersonalContact = false //
        val tactileOther = false //
        val tasteStrong = false //
        val tasteBad = false //
        val tasteOther = false //
        val tags = arrayListOf<String>()

        val logData = LogData(
            datetime,
            avgLux,
            luxStdev,
            lightOther,
            avgDecibels,
            noiseOther,
            smellStrong,
            smellOther,
            tactileBad,
            tactilePersonalContact,
            tactileOther,
            tasteStrong,
            tasteBad,
            tasteOther,
            tags
        )

        return logData
    }
}

/**
 * The APP
 */
@Composable
fun WearApp(goToPermissions: () -> Unit, createLog: (Instant) -> LogData) {
    //SETUP
    val navController = rememberSwipeDismissableNavController()
    var activeLog by remember { mutableStateOf<LogData?>(null) }
    val context = LocalContext.current
    val database = remember { SqLiteDatabase.getInstance(context) }

        AppNavigation(
            navController = navController,
            activeLog = activeLog,
            database = database,
            goToPermissions = goToPermissions,
            createLog = createLog,
            onLogUpdate = { updatedLog -> activeLog = updatedLog }
        )
    }



