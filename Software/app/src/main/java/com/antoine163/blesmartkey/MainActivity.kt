package com.antoine163.blesmartkey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BleSmartKeyTheme {
                BleSmartKeyApp()
            }
        }
    }
}
