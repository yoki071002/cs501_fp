package com.example.musicalcalendar


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavGraph
import com.example.musicalcalendar.navigation.NavGraph
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

