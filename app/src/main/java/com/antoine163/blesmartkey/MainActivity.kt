package com.antoine163.blesmartkey

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.antoine163.blesmartkey.data.DataModule
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

    /**
     * The DataModule instance used for dependency injection.
     * This property is initialized later and provides access to data sources and repositories.
     */
    private lateinit var dataModule: DataModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("BSK", "::onCreate")

        // Initialize the DataModule
        if (!this::dataModule.isInitialized) {
            dataModule = DataModule(application)
        }

        // Request bluetooth permissions
        requestBluetoothPermissions.launch(bluetoothPermissions)

        setContent {
            BleSmartKeyTheme {
                BleSmartKeyScreen(dataModule = dataModule)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BSK", "::onDestroy")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("BSK", "::onRestart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("BSK", "::onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("BSK", "::onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("BSK", "::onStop")
    }

    override fun onStart() {
        super.onStart()
        Log.d("BSK", "::onStart")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d("BSK", "::onLowMemory")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d("BSK", "::onTrimMemory")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("BSK", "::onSaveInstanceState: $outState")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d("BSK", "::onRestoreInstanceState: $savedInstanceState")
    }
}
