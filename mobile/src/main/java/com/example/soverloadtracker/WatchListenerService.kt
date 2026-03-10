/**
 * Communications service that handles sending messages to and receiving data from the connected watch
 */
package com.example.soverloadtracker


import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.soverloadtracker.dataStorage.LogData
import kotlinx.coroutines.tasks.await
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.lazy

class WatchListenerService : WearableListenerService() {
    private val database by lazy { SqLiteDatabase.getInstance(this)}

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