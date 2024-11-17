package com.antoine163.blesmartkey.ui

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antoine163.blesmartkey.ble.BleDevice
import com.antoine163.blesmartkey.ble.BleDeviceCallback
import com.antoine163.blesmartkey.data.DevicesBleSettingsRepository
import com.antoine163.blesmartkey.model.DeviceListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DevicesListUiState(
    val devices: List<DeviceListItem> = listOf()
)

@SuppressLint("MissingPermission")
class DevicesListViewModel(
    application: Application,
    devicesBleSettingsRepository : DevicesBleSettingsRepository
) : AndroidViewModel(application) {

    // MutableStateFlow to hold the UI state of the device scan
    private val _uiState = MutableStateFlow(DevicesListUiState())
    val uiState: StateFlow<DevicesListUiState> = _uiState.asStateFlow()

    // Bluetooth manager and scanner
    private val bluetoothManager = getApplication<Application>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner


    // Ble device to implement the openDoor function
    private var bleDevice: BleDevice? = null

    // BleDeviceCallback instance to handle callbacks from the BleDevice to implement the openDoor
    // function
    private val bleDeviceCallback = object : BleDeviceCallback() {

        // Handle connection state changes
        override fun onConnectionStateChanged(isConnected: Boolean) {
            if (isConnected)
                bleDevice?.openDoor()
            else
                bleDevice = null
        }

        // Handle lock state changes
        override fun onLockStateChanged(isLocked: Boolean) {}

        // Handle door state changes
        override fun onDoorStateChanged(isOpened: Boolean) {
            if (isOpened) {
                bleDevice?.disconnect()
            }else {
                bleDevice?.unlock()
                bleDevice?.openDoor()
            }

            // Update the UI state with the new list of devices
            bleDevice?.let { bleDevice ->
                _uiState.update { currentState ->
                    val updatedDevices = currentState.devices.map { device ->
                        if (device.address == bleDevice.getAddress()) {
                            device.copy(isOpened = isOpened)
                        } else {
                            device
                        }
                    }
                    currentState.copy(devices = updatedDevices)
                }
            }
        }

        // Handle current brightness read
        override fun onBrightnessRead(brightness: Float) {}

        // Handle brightness threshold read
        override fun onBrightnessThChanged(brightness: Float) {}

        // Handle device name changes
        override fun onDeviceNameChanged(deviceName: String) {}

        // Handle rssi changes
        override fun onRssiRead(rssi: Int) {}
    }

    /**
     * Opens the door associated with the given device address.
     *
     * This function attempts to connect to the BLE device using the provided address.
     * If a connection is already established, it will be reused.
     *
     * @param address The Bluetooth address of the door device.
     */
    fun openDoor(address: String) {
        /* TODO programmer un timeout */

        if (bleDevice != null && bleDevice?.getAddress() == address) {
            bleDevice?.unlock()
            bleDevice?.openDoor()
        } else {
            bleDevice = BleDevice(getApplication<Application>(), address, bleDeviceCallback)
            bleDevice?.connect()
        }
    }

    /**
     * Scan callback object that handles the results of Bluetooth LE scans.
     */
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d("BSK", "Scan result: ${result.device.name} - ${result.device.address} : ${result.rssi}")

            // Update the UI state with the new list of devices
            _uiState.update { currentState ->
                val updatedDevices = currentState.devices.map { device ->
                    if (device.address == result.device.address) {
                        device.copy(rssi = result.rssi, isOpened = false)
                    } else {
                        device
                    }
                }
                currentState.copy(devices = updatedDevices)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Handle scan failure
            Log.e("BSK", "Scan failed with error code: $errorCode")
        }
    }

    init {
        // Set scan settings
        val scanSettings: ScanSettings = ScanSettings.Builder()
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .setReportDelay(0L)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()




        viewModelScope.launch {
            // Create a list of ScanFilter objects for each device
            val scanFilters: MutableList<ScanFilter> = mutableListOf()


            // Collect the devices from the repository and convert them to a list of DeviceListItem objects
            val devicesBleSettings = devicesBleSettingsRepository.devicesFlow.first()

            // Convert the DevicesBleSettings object to a list of DeviceListItem objects
            val devices = devicesBleSettings.devicesList.map { deviceBleSettings ->

                // Create a ScanFilter for each device
                val scanFilter = ScanFilter.Builder()
                    .setDeviceAddress(deviceBleSettings.address)
                    .build()
                scanFilters.add(scanFilter)

                // Create a DeviceListItem object for each device
                DeviceListItem(
                    name = deviceBleSettings.name,
                    address = deviceBleSettings.address,
                    rssi = null,
                    isOpened = deviceBleSettings.wasOpened,
                )
            }

            // Update the UI state with the new list of devices
            _uiState.update { currentState ->
                currentState.copy( devices )
            }

            // Start scan
            bluetoothLeScanner.stopScan(scanCallback)
            if (scanFilters.isNotEmpty()) {
                //bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
                bluetoothLeScanner.startScan(scanCallback)
            }
        }
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * This function stops the Bluetooth LE scan.
     *
     * @SuppressLint("MissingPermission") - Permission check is handled before ViewModel initialization.
     */
    override fun onCleared() {
        super.onCleared()
        bluetoothLeScanner.stopScan(scanCallback)
    }
}

/**
 * Factory for creating [DevicesListViewModel] instances.
 *
 * This factory takes a [DevicesBleSettingsRepository] as a dependency and uses it to create
 * an instance of [DevicesListViewModel]. This allows for dependency injection and ensures
 * that the ViewModel has access to the necessary repository.
 *
 * @param devicesBleSettingsRepository The repository for accessing BLE settings for devices.
 */
class DevicesListViewModelFactory(
    private val application: Application,
    private val devicesBleSettingsRepository : DevicesBleSettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DevicesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DevicesListViewModel(application, devicesBleSettingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}