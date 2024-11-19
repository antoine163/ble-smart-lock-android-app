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
import kotlinx.coroutines.delay
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
    devicesBleSettingsRepository: DevicesBleSettingsRepository
) : AndroidViewModel(application) {

    // MutableStateFlow to hold the UI state of the device scan
    private val _uiState = MutableStateFlow(DevicesListUiState())
    val uiState: StateFlow<DevicesListUiState> = _uiState.asStateFlow()

    // Bluetooth manager and scanner
    private val bluetoothManager =
        getApplication<Application>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner

    // Ble device to implement the openDoor function
    private var bleDevice: BleDevice? = null

    // Map of device addresses to their last seen timestamp
    private val deviceLastSeen = mutableMapOf<String, Long>()
    private val timeoutMillis = 3000L // 3 secondes

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
            } else {
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
            Log.d(
                "BSK",
                "Scan result: ${result.device.name} - ${result.device.address} : ${result.rssi}"
            )

            // Update the last seen timestamp for the device
            deviceLastSeen[result.device.address] = System.currentTimeMillis()

            // Update the UI state with the new list of devices
            _uiState.update { currentUiState ->
                currentUiState.copy(devices = currentUiState.devices.map { uiDevice ->
                    if (uiDevice.address == result.device.address) {
                        uiDevice.copy(rssi = result.rssi, isOpened = false)
                    } else {
                        uiDevice
                    }
                })
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            super.onBatchScanResults(results)

            Log.d("BSK", "Batch scan results: ${results?.size} devices detected")

            // Log the address of each device found in the batch scan results
            results?.forEach { bleResult ->
                Log.d(
                    "BSK",
                    "Device address: ${bleResult?.device?.address}, name: ${bleResult?.device?.name}"
                )
            }

            // Update the UI state with the new list of devices
            _uiState.update { currentUiState ->
                // Create a map of results, keyed by device address, for efficient lookup
                val resultsMap = results?.associateBy { it?.device?.address } ?: emptyMap()
                // Update the UI devices with the latest RSSI values from the scan results
                val updatedUiDevices = currentUiState.devices.map { uiDevice ->
                    resultsMap[uiDevice.address]?.let { bleResult -> // 3 & 4
                        uiDevice.copy(rssi = bleResult.rssi, isOpened = false)
                    } ?: uiDevice
                }
                currentUiState.copy(devices = updatedUiDevices)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Handle scan failure
            Log.e("BSK", "Scan failed with error code: $errorCode")
        }
    }

    init {
        viewModelScope.launch {
            /* TODO relire la config suit a un changement de config d'un device ou ajout d'un device */

            // 1) Read configuration (list of device) from repository

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

            // 2) Update the UI state with the devices list
            _uiState.update { currentState ->
                currentState.copy(devices)
            }

            // 3) Start scanning
            bluetoothLeScanner.stopScan(scanCallback)
            if (scanFilters.isNotEmpty()) {
                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0)
                    .build()

                bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)


                // Loop indefinitely to check for device timeouts
                while (true) {
                    // Wait for 1 second before checking again
                    delay(1000)

                    // Get the current time
                    val currentTime = System.currentTimeMillis()

                    // Iterate through each device in the deviceLastSeen map
                    deviceLastSeen.entries.forEach { (address, lastSeen) ->
                        // Check if the device has exceeded the timeout period
                        if (currentTime - lastSeen > timeoutMillis) {
                            // If the device has timed out, update the UI state to remove its RSSI value
                            _uiState.update { currentState ->
                                currentState.copy(devices = currentState.devices.map { device ->
                                    if (device.address == address) {
                                        device.copy(rssi = null)
                                    } else device
                                })
                            }
                            // Remove the device from the deviceLastSeen map
                            deviceLastSeen.remove(address)
                        }
                    }
                }
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
    private val devicesBleSettingsRepository: DevicesBleSettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DevicesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DevicesListViewModel(application, devicesBleSettingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}