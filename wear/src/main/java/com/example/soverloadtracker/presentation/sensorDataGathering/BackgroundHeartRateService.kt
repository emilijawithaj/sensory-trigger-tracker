package com.example.soverloadtracker.presentation.sensorDataGathering

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import com.example.soverloadtracker.SqLiteDatabase
import com.example.soverloadtracker.presentation.LogData
import java.time.Instant

class BackgroundHeartRateService() : PassiveListenerService() {

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val hrPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        for (point in hrPoints) {
            val bpm = point.value
            Log.d("HRateBG", "Background BPM: $bpm")

            val database = SqLiteDatabase.getInstance(this)
            database.addLogRecord(LogData(Instant.now(), bpm.toFloat(), 0f, false, 0f, false, false, false, false, false, false, false, false, false, arrayListOf()))
        }
    }
}