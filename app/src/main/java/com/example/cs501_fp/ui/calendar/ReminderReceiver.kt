package com.example.cs501_fp.ui.calendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cs501_fp.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventName = intent.getStringExtra("eventName") ?: "Upcoming Event"

        val builder = NotificationCompat.Builder(context, "event_reminder_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Event Reminder")
            .setContentText("Don’t forget: $eventName starts soon!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(eventName.hashCode(), builder.build())
            }
        }

    }
}
