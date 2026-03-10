/**
 * Listener service that is always listening for incoming messages from the watch app
 */
package com.example.soverloadtracker.presentation


import android.util.Log
import com.example.soverloadtracker.SqLiteDatabase
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PhoneListenerService : WearableListenerService() {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val database by lazy { SqLiteDatabase.getInstance(this)}
        private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        Log.d(TAG, String(messageEvent.data))

        //handle incoming request for data sync
        if (messageEvent.path == "/sync") {
            syncLogsToPhone()
        }

        //handle message for log delete
        if (messageEvent.path == "/delete") {
            val dateTime = String(messageEvent.data, Charsets.UTF_8)
            database.deleteLog(dateTime)
            Log.d(TAG, "Deleted log with dateTime: $dateTime")
        }
    }

    /**
     * Send all logs in the database as DataItems to the phone
     */
    private fun syncLogsToPhone() {
        serviceScope.launch {
            //fetch database logs as list of LogData
            val logs = database.listLogRecords()
            if (logs.isEmpty()) {
                Log.d(TAG, "No logs to sync.")
                return@launch
            }

            try {
                //make DataMap for each log entry
                val logDataMapList = ArrayList<DataMap>()
                for (log in logs) {
                    val dataMap = DataMap().apply {
                        putString("datetime", log.dateTime.toString())
                        putFloat("avgLux", log.avgLux)
                        putFloat("luxStdev", log.luxStdev)
                        putBoolean("lightOther", log.lightOther)
                        putFloat("avgDecibels", log.avgDecibels)
                        putBoolean("noiseOther", log.noiseOther)
                        putBoolean("smellStrong", log.smellStrong)
                        putBoolean("smellOther", log.smellOther)
                        putBoolean("tactileBad", log.tactileBad)
                        putBoolean("tactilePersonalContact", log.tactilePersonalContact)
                        putBoolean("tactileOther", log.tactileOther)
                        putBoolean("tasteStrong", log.tasteStrong)
                        putBoolean("tasteBad", log.tasteBad)
                        putBoolean("tasteOther", log.tasteOther)
                        putStringArrayList("tags", log.tags)
                    }
                    logDataMapList.add(dataMap)
                }

                //send
                val putDataMapRequest = PutDataMapRequest.create("/syncLogs").apply {
                    dataMap.putDataMapArrayList("logList", logDataMapList)
                    // Add a timestamp to ensure the data item is always updated*TODO
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }

                val putDataRequest = putDataMapRequest.asPutDataRequest()
                dataClient.putDataItem(putDataRequest).await()

                Log.d(TAG, "Successfully sent ${logs.size} logs to the phone.")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to send log data to phone.", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "ListenerServiceOnWatch"
    }
}