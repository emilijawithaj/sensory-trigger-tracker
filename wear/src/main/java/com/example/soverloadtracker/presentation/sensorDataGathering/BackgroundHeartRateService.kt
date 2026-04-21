package com.example.soverloadtracker.presentation.sensorDataGathering

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import com.example.soverloadtracker.R
import com.example.soverloadtracker.presentation.dataStorage.SettingsManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BackgroundHeartRateService : PassiveListenerService() {

    internal lateinit var sensorReadManager: SensorReader //internal so test can see it
    internal lateinit var settingsManager: SettingsManager

    /**
     * Handle new data points received from the sensors
     */
    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val hrPoints = dataPoints.getData(DataType.HEART_RATE_BPM)

        //setup
        //for preintialised sensorReadManager when testing
        if (!::sensorReadManager.isInitialized) {
            sensorReadManager = SensorReader(this, MainScope())
        }
        if (!::settingsManager.isInitialized) {
            settingsManager = SettingsManager(applicationContext)
        }
        //val settingsManager = SettingsManager(applicationContext)
        //sensorReadManager = SensorReader(this, MainScope())
        sensorReadManager.takeLightReading()
        sensorReadManager.takeSoundReading()
        runBlocking { delay(3500L) }

        //get settings values
        val checkingForBright = runBlocking { settingsManager.brightLightFlow.first() }
        val checkingForStrobing = runBlocking { settingsManager.strobingLightFlow.first() }
        val checkingForLoud = runBlocking { settingsManager.loudSoundFlow.first() }

        //flags
        var bpmHighEnough = false

        //deal with points
            for (point in hrPoints) {
                val bpm = point.value
                Log.d("HRateBG", "Background BPM: $bpm")

                val highHRthreshold = SensorDataComputer.HIGH_HR_THRESHOLD

                if (bpm > highHRthreshold) {
                    bpmHighEnough = true
                    break
                }
            }

        //compare values
        if (bpmHighEnough && checkingForBright && sensorReadManager.dataProcessor.isLightBright()) {
            sendAlert(getString(R.string.high_hr_detected_with_bright_light_present))
        }

        if (bpmHighEnough && checkingForStrobing && sensorReadManager.dataProcessor.isLightStrobing()) {
            sendAlert(getString(R.string.high_hr_detected_with_strobing_light_present))
        }

        if (bpmHighEnough && checkingForLoud && sensorReadManager.dataProcessor.isLoudSound()) {
            sendAlert(getString(R.string.high_hr_detected_in_loud_environment))
        }
    }

    /**
     * Builds and triggers a notification sent to the user, including a vibration.
     * @param message Message of the notification
     * is internal for testing purposes
     */
    internal fun sendAlert(message: String) {
        val context = applicationContext
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "soverload_alerts"

        // create channel
        val channel = NotificationChannel(
            channelId,
            "SOverload Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.HR_service_alerts_description)
        }
        notificationManager.createNotificationChannel(channel)

        // construct notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(getString(R.string.potential_overload_alert))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        //trigger vibration
        val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator

        val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)

        // Show Notification
        notificationManager.notify(getString(R.string.potential_overload_alert).hashCode(), notification)

        Log.d("HRateBG", "Alert sent")
    }


}