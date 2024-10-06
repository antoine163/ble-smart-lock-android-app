package com.antoine163.blesmartkey.ui

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antoine163.blesmartkey.model.DeviceScanItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.listOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.sortedByDescending

/**
 * Represents the UI state for the device scan screen.
 *
 * @property devices A list of [DeviceScanItem] objects representing the discovered devices.
 */
data class DevicesScanUiState(
    val devices: List<DeviceScanItem> = listOf()
)

/**
 * ViewModel for the Devices Scan screen.
 * It handles scanning for Bluetooth devices and exposes the scan results as a StateFlow.
 * The scan process automatically starts when the ViewModel is initialized and lasts for 5 minutes.
 *
 * @param application The application context.
 */
@SuppressLint("MissingPermission")
class DevicesScanViewModel(application: Application) : AndroidViewModel(application) {

    // MutableStateFlow to hold the UI state of the device scan
    private val _uiState = MutableStateFlow(DevicesScanUiState())
    val uiState: StateFlow<DevicesScanUiState> = _uiState.asStateFlow()

    // Map to store the scanned devices
    private val scannedDevices: MutableMap<String, DeviceScanItem> = mutableMapOf()
    // Bluetooth manager and scanner
    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner

    /**
     * Scan callback object that handles the results of Bluetooth LE scans.
     */
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            // Add or update the device in the list
            scannedDevices[result.device.address] =
                DeviceScanItem(
                    name = result.device.name ?: "Unknown",
                    address = result.device.address,
                    rssi = result.rssi
                )

            _uiState.update { currentState ->
                currentState.copy(devices = scannedDevices.values.sortedByDescending { it.rssi })
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Handle scan failure
            Log.e("BSK", "Scan failed with error code: $errorCode")
        }
    }

    init {
        // Clean devices list
        scannedDevices.clear()
        _uiState.update { currentState ->
            currentState.copy(devices = listOf())
        }

        // Start scan for 5min
        bluetoothLeScanner.startScan(scanCallback)
        viewModelScope.launch {
            delay(5 * 60 * 1000)
            bluetoothLeScanner.stopScan(scanCallback)
        }
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * It is used here to stop the Bluetooth LE scan.
     */
    override fun onCleared() {
        super.onCleared()
        bluetoothLeScanner.stopScan(scanCallback)
    }
}