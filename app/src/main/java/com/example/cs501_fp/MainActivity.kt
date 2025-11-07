package com.example.musicalcalendar


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cs501_fp.ui.navigation.NavGraph
import com.example.musicalcalendar.ui.theme._501_fpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _501_fpTheme {
                NavGraph()
            }
        }
    }
}

