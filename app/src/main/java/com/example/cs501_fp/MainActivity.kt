// File: app/src/main/java/com/example/cs501_fp/MainActivity.kt
package com.example.cs501_fp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.cs501_fp.ui.navigation.NavGraph
import com.example.cs501_fp.viewmodel.ThemeViewModel
import com.example.cs501_fp.ui.theme._501_fpTheme

class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
