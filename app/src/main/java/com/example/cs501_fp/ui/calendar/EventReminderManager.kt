package com.example.cs501_fp.ui.calendar

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.cs501_fp.R

class EventReminderManager(private val context: Context) {

    /**
     * 创建事件提醒通知通道（仅需调用一次）
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
     * 请求通知权限（Android 13+）
     */
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (context is Activity) {
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }
            }
        }
    }

    /**
     * 在未来某个时间点触发系统闹钟（AlarmManager），发送广播给 ReminderReceiver，从而显示通知
     */
    fun scheduleEventReminder(eventId: Int, eventName: String, triggerAtMillis: Long) {
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
            // Android 12 及以上需要检查是否允许精确定时
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    Log.w("Reminder", "Exact alarms not allowed, using set instead.")
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                // Android 11 及以下直接使用
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

    }
}