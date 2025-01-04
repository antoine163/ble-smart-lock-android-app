package com.antoine163.blesmartkey

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.antoine163.blesmartkey.data.DataModule
import com.antoine163.blesmartkey.ui.BleSmartKeyScreen
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var autoUnlockService: AutoUnlockService
    private var autoUnlockServiceBound: Boolean = false

    private val dataModule: DataModule by lazy { (application as BskApplication).dataModule() }

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

    private val autoUnlockServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as AutoUnlockService.LocalBinder
            autoUnlockService = binder.getService()
            autoUnlockService.stop()
            autoUnlockServiceBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            autoUnlockServiceBound = false
            Log.e("BSK", "onServiceDisconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request bluetooth permissions
        requestBluetoothPermissions.launch(bluetoothPermissions)

        // Bind to the AutoUnlockService
        Intent(this, AutoUnlockService::class.java).also { intent ->
            bindService(intent, autoUnlockServiceConnection, BIND_AUTO_CREATE)
        }

        setContent {
            BleSmartKeyTheme {
                BleSmartKeyScreen(dataModule = dataModule)
            }
        }
    }

    override fun onStop() {
        super.onStop()

        // Save device list settings
        lifecycleScope.launch {
            dataModule.deviceListSettingsRepository().save()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (autoUnlockServiceBound == true) {
            // start the AutoUnlockService
            autoUnlockService.start()

            // Unbind from the AutoUnlockService
            unbindService(autoUnlockServiceConnection)
            autoUnlockServiceBound = false
        }
    }

}
