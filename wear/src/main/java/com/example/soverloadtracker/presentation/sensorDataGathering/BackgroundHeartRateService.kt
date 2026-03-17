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
import com.example.soverloadtracker.presentation.dataStorage.SettingsManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BackgroundHeartRateService : PassiveListenerService() {

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val hrPoints = dataPoints.getData(DataType.HEART_RATE_BPM)

        //setup
        val settingsManager = SettingsManager(applicationContext)

        val serviceScope = MainScope()
        val sensorReadManager = SensorReader(
            context = this,
            coroutineScope = serviceScope
        )
        runBlocking { sensorReadManager.takeLightReading() }
        runBlocking { sensorReadManager.takeSoundReading() }

        //get settings values
        val autoTriggerEnabled = runBlocking { settingsManager.autoTriggersFlow.first() }
        val checkingForBright = runBlocking { settingsManager.strobingLightFlow.first() }
        val checkingForStrobing = runBlocking { settingsManager.brightLightFlow.first() }
        val checkingForLoud = runBlocking { settingsManager.loudSoundFlow.first() }

        //deal with points
        for (point in hrPoints) {
            val bpm = point.value
            Log.d("HRateBG", "Background BPM: $bpm")

            val highHRthreshold = SensorDataComputer.HIGH_HR_THRESHOLD

            if (bpm > highHRthreshold) {
                //compare values
                if (checkingForBright) {
                    if (sensorReadManager.dataProcessor.isLightBright()) {
                        sendAlert(
                            "Potential Overload Alert!",
                            "High HR detected in bright environment"
                        )
                    }
                }



                if (checkingForStrobing) {
                    if (sensorReadManager.dataProcessor.isLightStrobing()) {
                        sendAlert(
                            "Potential Overload Alert!",
                            "High HR detected with strobing light present"
                        )

                    }
                }

                if (checkingForLoud) {
                    if (sensorReadManager.dataProcessor.isLoudSound()) {
                        sendAlert(
                            "Potential Overload Alert!",
                            "High HR detected in loud environment"
                        )
                    }
                }
            }
        }
    }

    /**
     * Builds and triggers a notification sent to the user, including a vibration.
     * @param title Title of the notification
     * @param message Message of the notification
     */
    private fun sendAlert(title: String, message: String) {
        val context = applicationContext
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "soverload_alerts"

        // create channel
        val channel = NotificationChannel(
            channelId,
            "SOverload Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for possible sensory overload occurrences."
        }
        notificationManager.createNotificationChannel(channel)

        // construct notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
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
        // Use a unique ID based on the title hash so multiple alerts can show at once
        notificationManager.notify(title.hashCode(), notification)
    }


}