package com.antoine163.blesmartkey

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.antoine163.blesmartkey.ble.BleAutoUnlockManager
import com.antoine163.blesmartkey.data.DevicesBleSettingsRepositoryApp
import com.antoine163.blesmartkey.model.DeviceAutoUnlok
import com.antoine163.blesmartkey.ui.BleSmartKeyScreen
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme

class MainActivity : ComponentActivity() {
    // Bluetooth permissions required for Android 12 (API 31) and above
    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADMIN,
    )

    private val requestBluetoothPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("BSK", "${it.key} = ${it.value}")
            }
        }

    private var bleAutoUnlockManager: BleAutoUnlockManager? = null



//
//
//    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
//        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
//            if (serviceClass.name == service.service.className) {
//                return true
//            }
//        }
//        return false
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        requestBluetoothPermissions.launch(bluetoothPermissions) // Request permissions


        val unlockDev = listOf(
            DeviceAutoUnlok("5A:9B:63:59:DC:8D", -60),
            DeviceAutoUnlok("11:22:33:44:55:66", -60),
            DeviceAutoUnlok("77:88:99:AA:BB:CC", -100)
        )
        //val unlockDev = emptyList<DeviceAutoUnlok>()

        bleAutoUnlockManager = BleAutoUnlockManager(this)
        bleAutoUnlockManager?.setUnlockDev(unlockDev)
//        if (!isServiceRunning(this, BleAutoUnlockService::class.java)) {
//            Intent(this, BleAutoUnlockService::class.java).also { intent ->
//                startForegroundService(intent)
//            }
//            Log.d("BSK", "Service started")
//        }
//        else {
//            Log.d("BSK", "Service already running")
//        }






        setContent {
            BleSmartKeyTheme {
                BleSmartKeyScreen(
                    devicesBleSettingsRepository = DevicesBleSettingsRepositoryApp(this)
                )
            }
        }
    }
}
