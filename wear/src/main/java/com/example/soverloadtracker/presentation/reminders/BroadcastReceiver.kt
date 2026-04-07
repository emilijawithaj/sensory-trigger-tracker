package com.example.soverloadtracker.presentation.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.soverloadtracker.presentation.PhoneListenerService

/**
 * Broadcast receiver to send message to connected device on notification tap
 */
class NotificationReceiver : BroadcastReceiver() {
    /**
     * Send mark end message to phone
     */
    override fun onReceive(context: Context, intent: Intent) {
        PhoneListenerService.sendMarkEnd(context)
        Log.d("NotificationReceiver", "MarkEnd message sent to phone via notification tap")
    }
}