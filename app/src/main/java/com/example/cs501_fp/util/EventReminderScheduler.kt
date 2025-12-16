package com.example.cs501_fp.util

import android.content.Context
import androidx.work.*
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.worker.EventReminderWorker
import java.util.concurrent.TimeUnit

object EventReminderScheduler {

    fun schedule(context: Context, event: UserEvent) {
        val startMillis = EventTimeUtil.toStartMillis(event.dateText, event.timeText) ?: return
        val reminderMillis = startMillis - 2 * 60 * 60 * 1000
        val delay = reminderMillis - System.currentTimeMillis()

        if (delay <= 0) return

        val data = workDataOf(
            "title" to event.title,
            "venue" to event.venue
        )

        val request = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(event.id) // ⭐ 用 event.id 作为 tag，方便取消
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancel(context: Context, eventId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(eventId)
    }
}
