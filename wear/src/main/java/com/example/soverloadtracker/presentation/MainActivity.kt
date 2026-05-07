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
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.soverloadtracker.SqLiteDatabase
import com.example.soverloadtracker.presentation.dataStorage.LogData
import com.example.soverloadtracker.presentation.dataStorage.ThresholdData
import com.example.soverloadtracker.presentation.screens.AppNavigation
import com.example.soverloadtracker.presentation.sensorDataGathering.SensorDataComputer
import com.example.soverloadtracker.presentation.sensorDataGathering.SensorReader
import com.example.soverloadtracker.presentation.theme.AppTheme
import java.time.Instant


class MainActivity : ComponentActivity() {
    private val sensorReadManager by lazy {
        SensorReader(
            context = this,
            coroutineScope = lifecycleScope
        )
    }

    @RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 13)
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(style.Theme_DeviceDefault)
        //val userDB = SqLiteDatabase.getInstance(this)
        //userDB.onUpgrade(userDB.writableDatabase, 1, 1)
        updateThresholds()

        setContent {
            AppTheme {
                WearApp(
                    {
                        checkPermissions(
                            arrayListOf(
                                HealthPermissions.READ_HEART_RATE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.BODY_SENSORS_BACKGROUND,
                                Manifest.permission.BODY_SENSORS,
                                Manifest.permission.POST_NOTIFICATIONS,
                                HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND
                            )
                        )
                    },
                    { createLog(Instant.now()) })
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
            sensorReadManager.dataProcessor.isLightBright(),
            luxStdev,
            lightOther,
            avgDecibels,
            sensorReadManager.dataProcessor.isLoudSound(),
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

    /**
     * Updates sensor 'high value' threshold using Bayesian Running Probability
     */
    fun updateThresholds() {
        val userDB = SqLiteDatabase.getInstance(this)

        //get thresholds
        val lightThreshold = SensorDataComputer.HIGH_LIGHT_LEVEL
        val soundThreshold = SensorDataComputer.DECIBEL_THRESHOLD

        //extract data from logs
        val logs = userDB.listLogRecords()

        val brightRecords = logs.map { log -> ThresholdData(log.avgLux, log.wasBright) }
        val loudRecords = logs.map { log -> ThresholdData(log.avgDecibels, log.wasLoud) }

        val trueBrightRecords = brightRecords.filter { it.consideredPresent }
        val falseBrightRecords = brightRecords.filter { !it.consideredPresent }

        val trueLoudRecords = loudRecords.filter { it.consideredPresent }
        val falseLoudRecords = loudRecords.filter { !it.consideredPresent }

        //ensure min sample size
        if (trueBrightRecords.size > 1 && falseBrightRecords.size > 1) {
            SensorDataComputer.HIGH_LIGHT_LEVEL =
                thresholdSweep(falseBrightRecords, trueBrightRecords, lightThreshold)
        }
        if (trueLoudRecords.size > 1 && falseLoudRecords.size > 1) {
            SensorDataComputer.DECIBEL_THRESHOLD =
                thresholdSweep(falseLoudRecords, trueLoudRecords, soundThreshold.toFloat()).toInt()
        }
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
        createLog = createLog
    )
}

/**
 * Function to perform threshold sweep to update sensor thresholds.
 * @param belowRecords existing records that should return false
 * @param aboveRecords existing records that should return true
 * @param threshold current threshold value
 * @return new threshold
 */
fun thresholdSweep(belowRecords: List<ThresholdData>, aboveRecords: List<ThresholdData>, threshold: Float): Float {
    //setup
    val sortedRecords = (belowRecords + aboveRecords).sortedBy { it.value }
    var maxMargin = -1.0f
    var highestAccuracyFound = 0
    var bestThreshold = threshold

    for (i in 0 until sortedRecords.size - 1) {

        //propose a candidate
        val candidate = (sortedRecords[i].value + sortedRecords[i + 1].value) / 2

        //count candidate errors
        val correctlyClassified = (aboveRecords.count { it.value >= candidate } +
                belowRecords.count { it.value < candidate })

        //find gap between points seperated
        val margin = sortedRecords[i + 1].value - sortedRecords[i].value

        //save if best option so far
        if (correctlyClassified > highestAccuracyFound ||
            (correctlyClassified == highestAccuracyFound && margin > maxMargin)) {

            highestAccuracyFound = correctlyClassified
            maxMargin = margin
            bestThreshold = candidate
        }
    }

    //select only if more than 80%
    //use Bayesian running probability to smooth results
    return if (highestAccuracyFound >= sortedRecords.size * 0.8) {
        val alpha = 0.3f
        (threshold * (1 - alpha)) + (bestThreshold * alpha)
    } else {
        threshold
    }
}



