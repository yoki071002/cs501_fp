package com.example.cs501_fp.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object EventTimeUtil {

    fun toStartMillis(dateText: String, timeText: String): Long? {
        return try {
            val date = LocalDate.parse(dateText)
            val time = LocalTime.parse(timeText)
            date.atTime(time)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }
}