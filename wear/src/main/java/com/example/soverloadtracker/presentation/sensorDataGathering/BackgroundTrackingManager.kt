package com.example.soverloadtracker.presentation.sensorDataGathering

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig

class BackgroundTrackingManager(context: Context) {
    private val healthClient = HealthServices.getClient(context)
    private val passiveClient = healthClient.passiveMonitoringClient

    /**
     * Begins listening for heart rate data in the background.
     */
    suspend fun startHRTracking() {
        val passiveListenerConfig = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.HEART_RATE_BPM))
            .build()

        passiveClient.setPassiveListenerServiceAsync(
            BackgroundHeartRateService::class.java,
            passiveListenerConfig
        ).await()
        Log.d("HRateBG", "Background Tracking Started")
    }

    /**
     * Stops the background heart rate tracking service.
     */
    fun stopHRTracking() {
        passiveClient.clearPassiveListenerServiceAsync()
    }
}