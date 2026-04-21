/**
 * Communications service that handles sending messages to and receiving data from the connected watch
 */
package com.example.soverloadtracker


import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.soverloadtracker.dataStorage.LogData
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant

class WatchListenerService : WearableListenerService() {
    private val database by lazy { SqLiteDatabase.getInstance(this)}

    /**
     * Handles incoming messages from the watch
     */
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): ${messageEvent.path}")
        //Log.d(TAG, String(messageEvent.data))

        //handle automatic factor background tracking being turned on or off
        if (messageEvent.path == "/autoTracking") {
            val prefs = getSharedPreferences("SOverloadSettings", MODE_PRIVATE)

            prefs.edit().apply {
                putBoolean("autoTracking", String(messageEvent.data) == "true")
                apply()
            }

            //do initial set
            if (prefs.getBoolean("autoTracking", true)) {
                autoSettingsSet()
            }
        }

        //handle edit launch on mark end
        if (messageEvent.path == "/markEnd") {
            Log.d(TAG, "Mark end received.")
            val startIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("markEnd", true)
            }
            startActivity(startIntent)
        }
    }

    /**
     * Checks for factors to be autotracked and triggers a sending of them to the watch
     */
    fun autoSettingsSet() {
        val highFrequencyThreshold = 60

        val triggerList = database.getTriggers()
        val allLogs = database.listLogRecords()
        val frequencyMap = FrequencyCalcHelper.calculateFactorPercentages(this, allLogs)

        var brightLight = false
        var strobeLight = false
        var loudSound = false

        //check trigger db
        if (triggerList.contains(getString(R.string.factor_brightness))) {
            brightLight = true
        }
        if (triggerList.contains(getString(R.string.factor_strobing))) {
            strobeLight = true
        }
        if (triggerList.contains(getString(R.string.factor_loud))) {
            loudSound = true
        }

        //check by frequency
        if (frequencyMap[getString(R.string.factor_brightness)]!! > highFrequencyThreshold) {
            brightLight = true
        }
        if (frequencyMap[getString(R.string.factor_strobing)]!! > highFrequencyThreshold) {
            strobeLight = true
        }
        if (frequencyMap[getString(R.string.factor_loud)]!! > highFrequencyThreshold) {
            loudSound = true
        }

        sendSettingsUpdate(this, brightLight, strobeLight, loudSound)
    }

    /**
     * Handles incoming database logs from the watch
     */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged(): $dataEvents")
        dataEvents.forEach { event ->
            if (event.type == com.google.android.gms.wearable.DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/syncLogs") {

                //extract datamaps
                try {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val logDataMapList = dataMapItem.dataMap.getDataMapArrayList("logList")

                    if (logDataMapList == null) {
                        Log.w(TAG, "Received data but log_list was null.")
                        return@forEach
                    }

                    //convert to LogData objects
                    val receivedLogs = logDataMapList.map { dataMap ->
                        LogData(
                            dateTime = Instant.parse(dataMap.getString("datetime")),
                            avgLux = dataMap.getFloat("avgLux"),
                            wasBright = dataMap.getBoolean("wasBright"),
                            wasLoud = dataMap.getBoolean("wasLoud"),
                            luxStdev = dataMap.getFloat("luxStdev"),
                            lightOther = dataMap.getBoolean("lightOther"),
                            avgDecibels = dataMap.getFloat("avgDecibels"),
                            noiseOther = dataMap.getBoolean("noiseOther"),
                            smellStrong = dataMap.getBoolean("smellStrong"),
                            smellOther = dataMap.getBoolean("smellOther"),
                            tactileBad = dataMap.getBoolean("tactileBad"),
                            tactilePersonalContact = dataMap.getBoolean("tactilePersonalContact"),
                            tactileOther = dataMap.getBoolean("tactileOther"),
                            tasteStrong = dataMap.getBoolean("tasteStrong"),
                            tasteBad = dataMap.getBoolean("tasteBad"),
                            tasteOther = dataMap.getBoolean("tasteOther"),
                            tags = dataMap.getStringArrayList("tags") ?: arrayListOf()
                        )
                    }

                    Log.d(TAG, "Successfully received and parsed ${receivedLogs.size} logs.")

                    //check for new logs
                    val existingLogs = database.listLogRecords()
                    for (log in receivedLogs) {
                        if (existingLogs.none { it.dateTime == log.dateTime }) {
                            database.addLogRecord(log)
                        }
                    }

                    val intent = Intent("com.example.soverloadtracker.SYNC_COMPLETE")
                    intent.setPackage(packageName)
                    sendBroadcast(intent)

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing received log data.", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "ListenerServiceOnPhone"

        /**
         * Sends request for the database to connected watch
         */
        fun sendSyncRequest(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                        val nodeClient = Wearable.getNodeClient(context)
                        val messageClient = Wearable.getMessageClient(context)
                        val nodes = nodeClient.connectedNodes.await()

                        for (node in nodes) {
                            messageClient.sendMessage(node.id, "/sync", "sync".toByteArray()).await()
                            Log.d(TAG, "Sent sync request to node: ${node.displayName}")
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending sync request.", e)
                }
            }
        }

        /**
         * Updates the settings for what should be tracked in the automatic factor selection
         * for background tracking/alert system
         */
        fun sendSettingsUpdate(context: Context, brightLight: Boolean, strobeLight: Boolean, loudSound: Boolean) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val nodeClient = Wearable.getNodeClient(context)
                    val messageClient = Wearable.getMessageClient(context)
                    val nodes = nodeClient.connectedNodes.await()

                    val payload = "$brightLight,$strobeLight,$loudSound".toByteArray(Charsets.UTF_8)

                    for (node in nodes) {
                        messageClient.sendMessage(node.id, "/tracking", payload).await()
                        Log.d(TAG, "Synced tracking factors to node: ${node.displayName}, payload: $payload")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending settings update request", e)
                }
            }
        }


        /**
         * Sends request to delete a log from the connected watch by its dateTime ID
         * @param dateTime ID of the log to be deleted
         */
        fun sendDeleteRequest(context: Context, dateTime: String) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val nodeClient = Wearable.getNodeClient(context)
                    val messageClient = Wearable.getMessageClient(context)
                    val nodes = nodeClient.connectedNodes.await()

                    for (node in nodes) {
                        messageClient.sendMessage(node.id, "/delete", dateTime.toByteArray((Charsets.UTF_8)))
                            .await()
                        Log.d(TAG, "Sent delete request to node: ${node.displayName}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending delete request.", e)
                }
            }
        }
    }
}