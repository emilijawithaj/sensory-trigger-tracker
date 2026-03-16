package com.example.soverloadtracker.presentation.sensorDataGathering

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType

class BackgroundHeartRateService() : PassiveListenerService() {
    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val hrPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        for (point in hrPoints) {
            val bpm = point.value
            Log.d("HR", "Background BPM: $bpm")

            //TODO handle
        }
    }
}