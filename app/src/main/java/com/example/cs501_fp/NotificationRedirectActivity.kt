package com.example.cs501_fp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.cs501_fp.MainActivity

/**
 * NotificationRedirectActivity
 * -------------------------------------------------
 * This activity serves as an entry point when a notification is clicked.
 * It receives the extras from the reminder and forwards them to MainActivity.
 */
class NotificationRedirectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventName = intent?.getStringExtra("eventName")

        // ✅ 创建跳转 Intent（把 eventName 带过去）
        val redirectIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fromReminder", true)
            putExtra("eventName", eventName)
        }

        startActivity(redirectIntent)
        finish() // 不显示自己
    }
}
