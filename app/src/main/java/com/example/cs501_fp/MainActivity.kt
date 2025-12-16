// File: app/src/main/java/com/example/cs501_fp/MainActivity.kt
package com.example.cs501_fp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cs501_fp.ui.navigation.NavGraph
import com.example.cs501_fp.viewmodel.ThemeViewModel
import com.example.cs501_fp.ui.theme._501_fpTheme
import com.example.cs501_fp.util.EventReminderScheduler
import com.example.cs501_fp.util.NotificationHelper

class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Android 13+ 请求通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        NotificationHelper.createChannel(this)

        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            _501_fpTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                NavGraph(themeViewModel = themeViewModel)
            }
        }
    }
}
