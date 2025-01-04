package com.antoine163.blesmartkey.ui

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antoine163.blesmartkey.ble.BleDevice
import com.antoine163.blesmartkey.ble.BleDeviceCallback
import com.antoine163.blesmartkey.copy
import com.antoine163.blesmartkey.data.DataModule
import com.antoine163.blesmartkey.data.model.DeviceListItem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class DevicesListUiState(
    val devices: List<DeviceListItem> = listOf()
)

@SuppressLint("MissingPermission")
class DeviceListViewModel(
    private val dataModule: DataModule
) : ViewModel() {

    // MutableStateFlow to hold the UI state of the device scan
    private val _uiState = MutableStateFlow(DevicesListUiState())
    val uiState: StateFlow<DevicesListUiState> = _uiState.asStateFlow()

    // Bluetooth scanner
    private val bluetoothLeScanner = dataModule.bluetoothManager().adapter.bluetoothLeScanner


    // Map of device addresses to their last seen timestamp
    private val deviceLastSeen = mutableMapOf<String, Long>()
    private val timeoutMillis = 5000L // 5 secondes

    /**
     * Opens the door associated with the given device address.
     *
     * This function attempts to connect to the BLE device using the provided address.
     * If a connection is already established, it will be reused.
     *
     * @param address The Bluetooth address of the door device.
     */
    fun openDoor(address: String) {
        var deviceFound = false

        // Update the UI state to indicate that the door is being opened
        _uiState.update { currentUiState ->
            currentUiState.copy(devices = currentUiState.devices.map { device ->
                if (device.address == address) {
                    deviceFound = true
                    device.copy(
                        isOpening = true,
                        isOpenTimeout = false
                    )
                } else device
            })
        }

        // Check if the device is already connected
        if (deviceFound == true) {
            viewModelScope.launch {
                // Deffered object to wait for the door to be opened
                val deferred = CompletableDeferred<Unit>()

                var bleDevice: BleDevice? = null
                var bleRssi: Int? = null
                var bleDeviceCallback = object : BleDeviceCallback() {
                    override fun onConnectionStateChanged(bleDevice: BleDevice, isConnected: Boolean) {
                        // If the device is connected, read the door state
                        if (isConnected) {
                            bleDevice.readRssi()
                            bleDevice.readDoorState()
                        }
                    }

                    override fun onDoorStateChanged(bleDevice: BleDevice, isOpened: Boolean) {
                        // If the door is closed, open the door
                        if (isOpened == false) {
                            bleDevice.openDoor()
                        } else {
                            // Completes the deferred object to indicate that the door is opened
                            deferred.complete(Unit)
                        }
                    }

                    override fun onRssiRead(bleDevice: BleDevice, rssi: Int) {
                        bleRssi = rssi
                    }
                }

                // 5 seconds timeout to open the door
                val result = withTimeoutOrNull(10000) {

                    bleDevice = BleDevice(dataModule.context, address, bleDeviceCallback)
                    bleDevice.connect()

                    // Wait for the door to be opened
                    deferred.await()
                }

                bleDevice?.disconnect()

                // Update the UI state to indicate that the door is opened or timeout.
                _uiState.update { currentUiState ->
                    currentUiState.copy(devices = currentUiState.devices.map { device ->
                        if (device.address == address) {
                            // Update the last seen timestamp for the device
                            deviceLastSeen[device.address] = System.currentTimeMillis()

                            device.copy(
                                isOpened = result != null,
                                isOpening = false,
                                isOpenTimeout = result == null,
                                rssi = bleRssi
                            )
                        } else device
                    })
                }
            }
        }
    }

    /**
     * Scan callback object that handles the results of Bluetooth LE scans.
     */
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            // Extract device name from advertising data
            val bleDevName =
                result.scanRecord?.advertisingDataMap?.get(0x09)?.let { byteArray ->
                    String(byteArray, Charsets.UTF_8)
                } ?: result.device?.name ?: "Unknown"

            // Extract device door state from advertising data
            val isDoorOpened =
                result.scanRecord?.advertisingDataMap?.get(0x2D)
                    ?.takeIf { it.size >= 3 }
                    ?.let { it[2].toInt() == 0x01 } == true

//            Log.d(
//                "BSK",
//                "${result.device.address} -> Scanned : '$bleDevName' \t ${result.rssi}dbm"
//            )

            // Update the last seen timestamp for the device
            deviceLastSeen[result.device.address] = System.currentTimeMillis()

            var updateSetting = false

            // Update the UI state with the new list of devices
            _uiState.update { currentUiState ->
                currentUiState.copy(devices = currentUiState.devices.map { uiDevice ->
                    if (uiDevice.address == result.device.address) {
                        updateSetting =
                            (uiDevice.isOpened != isDoorOpened) || (uiDevice.name != bleDevName)
                        uiDevice.copy(
                            name = bleDevName,
                            rssi = result.rssi,
                            isOpened = isDoorOpened
                        )
                    } else {
                        uiDevice
                    }
                })
            }

            // If isBleDoorOpen is different with de setting, update the setting
            if (updateSetting == true) {
                viewModelScope.launch {
                    val deviceSettings =
                        dataModule.deviceListSettingsRepository().getDevice(result.device.address)
                    deviceSettings?.let { deviceSettings ->
                        val deviceSettingsUpdated = deviceSettings.copy {
                            this.name = bleDevName
                            this.wasOpened = isDoorOpened
                        }
                        dataModule.deviceListSettingsRepository()
                            .updateDevice(deviceSettingsUpdated)
                    }
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            super.onBatchScanResults(results)
//
//            // Log the address of each device found in the batch scan results
//            Log.d("BSK", "Batch scan results: ${results?.size} devices detected:")
//            results?.forEach { bleResult ->
//
//                // Extract device name from advertising data
//                val bleDevName =
//                    bleResult?.scanRecord?.advertisingDataMap?.get(0x09)?.let { byteArray ->
//                        String(byteArray, Charsets.UTF_8)
//                    } ?: bleResult?.device?.name ?: "Unknown"
//
//                // Extract device door state from advertising data
//                val isBleDoorOpen =
//                    bleResult?.scanRecord?.advertisingDataMap?.get(0x2D)
//                        ?.takeIf { it.size >= 3 }
//                        ?.let { it[2].toInt() == 0x01 } == true
//
//                Log.d(
//                    "BSK",
//                    "    > $bleDevName - ${bleResult?.device?.address} : ${bleResult?.rssi}"
//                )
//            }
//
//            // Update the UI state with the new list of devices
//            _uiState.update { currentUiState ->
//                // Create a map of results, keyed by device address, for efficient lookup
//                val resultsMap = results?.associateBy { it?.device?.address } ?: emptyMap()
//                // Update the UI devices with the latest RSSI values from the scan results
//                val updatedUiDevices = currentUiState.devices.map { uiDevice ->
//                    resultsMap[uiDevice.address]?.let { bleResult -> // 3 & 4
//                        uiDevice.copy(rssi = bleResult.rssi, isOpened = false)
//                    } ?: uiDevice
//                }
//                currentUiState.copy(devices = updatedUiDevices)
//            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Handle scan failure
            Log.e("BSK", "Scan failed with error code: $errorCode")
        }
    }

    init {
        // Coroutine to read BLE device setting list and update device to scan
        viewModelScope.launch {
            // Create a list of ScanFilter objects for each device
            val scanFilters: MutableList<ScanFilter> = mutableListOf()

            // Collect the list of setting devices from repository
            dataModule.deviceListSettingsRepository().deviceListSettingsFlow.collectLatest { deviceListSettings ->

                // Convert the DevicesBleSettings object to a list of DeviceListItem objects and create a list of ScanFilter objects
                val deviceList = deviceListSettings.devicesList.map { deviceSettings ->

                    // Create a ScanFilter for each device
                    val scanFilter = ScanFilter.Builder()
                        .setDeviceAddress(deviceSettings.address)
                        .build()
                    scanFilters.add(scanFilter)

                    // Find if ble device already existent
                    val uiBleDevice =
                        uiState.value.devices.find { item -> item.address == deviceSettings.address }

                    // Create a DeviceListItem object for each device
                    DeviceListItem(
                        name = deviceSettings.name,
                        address = deviceSettings.address,
                        rssi = uiBleDevice?.rssi,
                        isOpened = deviceSettings.wasOpened,
                        isOpening = false,
                        isOpenTimeout = false
                    )
                }

                // Update the UI state with the devices list
                _uiState.update { currentState ->
                    currentState.copy(deviceList)
                }

                // Scanning BLE device with new list
                bluetoothLeScanner.stopScan(scanCallback)
                if (scanFilters.isNotEmpty()) {
                    val scanSettings = ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build()
                    bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
                }
            }
        }

        // Coroutine to detect devices that become inaccessible
        viewModelScope.launch {
            // Loop indefinitely to check for device timeouts
            while (true) {
                // Wait for 1 second before checking again
                delay(1000)

                // Get the current time
                val currentTime = System.currentTimeMillis()

                // Iterate through the deviceLastSeen map and remove devices that have timed out
                // We use an iterator to safely remove elements while iterating
                val iterator = deviceLastSeen.iterator()
                while (iterator.hasNext()) {
                    val (address, lastSeen) = iterator.next()
                    // If the device has not been seen in the timeout period, remove it
                    if (currentTime - lastSeen > timeoutMillis) {
                        _uiState.update { currentState ->
                            currentState.copy(devices = currentState.devices.map { device ->
                                if (device.address == address) {
                                    device.copy(rssi = null)
                                } else device
                            })
                        }
                        // Remove the device from the deviceLastSeen map
                        iterator.remove()
                    }
                }
            }
        }
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * This function stops the Bluetooth LE scan.
     */
    override fun onCleared() {
        super.onCleared()
        bluetoothLeScanner.stopScan(scanCallback)
    }
}

class DevicesListViewModelFactory(
    private val dataModule: DataModule,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceListViewModel(dataModule) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}