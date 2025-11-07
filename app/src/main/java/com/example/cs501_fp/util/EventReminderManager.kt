package com.example.cs501_fp.util

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.cs501_fp.util.ReminderReceiver

class EventReminderManager(private val context: Context) {

    /**
     * ✅ 创建通知通道（仅需调用一次，例如在 MainActivity.onCreate 中）
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "event_reminder_channel",
                "Event Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming events"
            }
            val manager = NotificationManagerCompat.from(context)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * ✅ Android 13+ 动态请求通知权限
     */
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            } else {
                Log.w("ReminderManager", "Context is not an Activity — cannot request permission.")
            }
        }
    }

    /**
     * ✅ 调度事件提醒（提前两小时触发）
     */
    fun scheduleEventReminder(eventId: Int, eventName: String, triggerAtMillis: Long) {
        val adjustedTrigger = triggerAtMillis - 2 * 60 * 60 * 1000  // 提前两小时

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("eventName", eventName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, adjustedTrigger, pendingIntent)
                } else {
                    Log.w("ReminderManager", "Exact alarms not allowed — using set().")
                    alarmManager.set(AlarmManager.RTC_WAKEUP, adjustedTrigger, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, adjustedTrigger, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e("ReminderManager", "Failed to schedule reminder: ${e.message}")
        }
    }

    /**
     * ✅ 取消指定事件提醒（防止重复或删除事件时清理）
     */
    fun cancelEventReminder(eventId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
