package com.example.soverloadtracker.presentation.reminders

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.soverloadtracker.R

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    /**
     * send a notification about a log that may be unfinished if the user never presses the end button
     */
    @SuppressLint("LaunchActivityFromNotification")
    override fun doWork(): Result {
        //notification sending
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "reminder_channel"
        val channel =
            NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        //set up click handling
        val context = applicationContext
        val intent = Intent(context, NotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.reminder_notif_title))
            .setContentText(context.getString(R.string.reminder_notif_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)


        return Result.success()
    }
}