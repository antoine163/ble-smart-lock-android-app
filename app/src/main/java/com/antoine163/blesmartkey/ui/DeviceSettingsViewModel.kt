package com.antoine163.blesmartkey.ui

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antoine163.blesmartkey.DeviceSettings
import com.antoine163.blesmartkey.ble.BleDevice
import com.antoine163.blesmartkey.ble.BleDeviceCallback
import com.antoine163.blesmartkey.data.DataModule
import com.antoine163.blesmartkey.data.model.DeviceSettingsItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.antoine163.blesmartkey.R

/**
 * UI state for representing the state of a device setting.
 * @property setting The device setting data. Defaults to an empty [DeviceSettingsItem].
 */
data class DeviceSettingsUiState(
    val setting: DeviceSettingsItem = DeviceSettingsItem(
        name = "Unknown",
        address = "",
        currentRssi = null,
        isOpened = false,
        isUnlocked = false,
        thresholdNight = 50f,
        currentBrightness = null,
        autoUnlockEnabled = false,
        autoUnlockRssiTh = -40,
        connectionErrorMessage = null
    )
)

/**
 * ViewModel for managing the settings of a Bluetooth device.
 *
 * This ViewModel handles the UI state of the device setting screen and interacts with the
 * BleDevice class to control the device's behavior. It also saves and loads device settings
 * from persistent storage.
 *
 * @param dataModule The data module providing access to application resources.
 * @param deviceAdd The Bluetooth address of the device.
 */
class DeviceSettingsViewModel(
    private val dataModule: DataModule,
    private val deviceAdd: String,
) : ViewModel() {

    // MutableStateFlow to hold the UI state of the device setting
    private val _uiState = MutableStateFlow(
        DeviceSettingsUiState(
            setting = DeviceSettingsItem(
                name = "Unknown",
                address = deviceAdd,
                currentRssi = null,
                isOpened = false,
                isUnlocked = false,
                thresholdNight = 50f,
                currentBrightness = null,
                autoUnlockEnabled = false,
                autoUnlockRssiTh = -40,
                connectionErrorMessage = null
            )
        )
    )
    val uiState: StateFlow<DeviceSettingsUiState> = _uiState.asStateFlow()

    // Job manage timer of connection state
    private var connectionStateJob: Job? = null

    /**
     * Enables or disables the auto-unlock feature.
     *
     * This function updates the UI state and saves the updated setting to persistent storage.
     *
     * @param enable `true` to enable auto-unlock, `false` to disable it.
     */
    fun autoUnlock(enable: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(setting = currentState.setting.copy(autoUnlockEnabled = enable))
        }

        saveDeviceSetting(_uiState.value.setting)
    }

    /**
     * Sets the RSSI threshold for automatic unlocking.
     *
     * This function updates the UI state with the new RSSI threshold and saves the updated device settings.
     *
     * @param rssiTh The new RSSI threshold value.
     */
    fun setAutoUnlockRssiTh(rssiTh: Int) {
        _uiState.update { currentState ->
            currentState.copy(setting = currentState.setting.copy(autoUnlockRssiTh = rssiTh))
        }
        saveDeviceSetting(_uiState.value.setting)
    }


    /**
     * Saves the device settings to the repository.
     *
     * This function updates the device settings in the repository based on the provided
     * [DeviceSettingsItem] object. It launches a coroutine within the viewModelScope
     * to perform the update operation.
     *
     * @param device The [DeviceSettingsItem] containing the updated device settings.
     */
    private fun saveDeviceSetting(device: DeviceSettingsItem) {
        viewModelScope.launch {
            dataModule.deviceListSettingsRepository().updateDevice(
                DeviceSettings.newBuilder()
                    .setName(device.name)
                    .setAddress(device.address)
                    .setWasOpened(device.isOpened)
                    .setAutoUnlockEnabled(device.autoUnlockEnabled)
                    .setAutoUnlockRssiTh(device.autoUnlockRssiTh)
                    .build()
            )
        }
    }

    // BleDeviceCallback instance to handle callbacks from the BleDevice
    private val bleDeviceCallback = object : BleDeviceCallback() {

        // Handle connection state changes
        override fun onConnectionStateChanged(bleDevice: BleDevice, isConnected: Boolean) {

            // Cancel job connection state
            connectionStateJob?.cancel()

            if (isConnected) {
                // If connected read deferment priority
                bleDevice.readDeviceName()
                bleDevice.readDoorState()
                bleDevice.readBrightnessTh()

                bleDevice.readRssi()
                bleDevice.readBrightness()
            } else {
                _uiState.update { currentState ->
                    currentState.copy(setting = currentState.setting.copy(currentRssi = null))
                }
            }
        }

        override fun onConnectionStateFailed(bleDevice: BleDevice) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(
                    connectionErrorMessage = R.string.connection_error_state,
                    currentRssi = null))
            }
        }

        // Handle lock state changes
        override fun onLockStateChanged(bleDevice: BleDevice, isLocked: Boolean) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(isUnlocked = !isLocked))
            }
        }

        // Handle door state changes
        override fun onDoorStateChanged(bleDevice: BleDevice, isOpened: Boolean) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(isOpened = isOpened))
            }

            // Update the device setting in the repository
            saveDeviceSetting(_uiState.value.setting)
        }

        // Handle current brightness read
        override fun onBrightnessRead(bleDevice: BleDevice, brightness: Float) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(currentBrightness = brightness))
            }

            // Read the brightness again after a delay
            viewModelScope.launch {
                delay(800)
                bleDevice.readBrightness()
            }
        }

        // Handle brightness threshold read
        override fun onBrightnessThChanged(bleDevice: BleDevice, brightness: Float) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(thresholdNight = brightness))
            }

            // Update the device setting in the repository
            saveDeviceSetting(_uiState.value.setting)
        }

        // Handle device name changes
        override fun onDeviceNameChanged(bleDevice: BleDevice, deviceName: String) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(name = deviceName))
            }

            // Update the device setting in the repository
            saveDeviceSetting(_uiState.value.setting)
        }

        // Handle rssi changes
        override fun onRssiRead(bleDevice: BleDevice, rssi: Int) {
            _uiState.update { currentState ->
                currentState.copy(
                    setting = currentState.setting.copy(
                        currentRssi = rssi
                    )
                )
            }

            // Read the rssi again after a delay
            viewModelScope.launch {
                delay(800)
                bleDevice.readRssi()
            }
        }
    }

    // Public BleDevice instance to interact with the Bluetooth device
    val bleDevice: BleDevice =
        BleDevice(dataModule.context, deviceAdd, bleDeviceCallback)

    /**
     * Dissociates from the current BLE device.
     *
     * This function performs the following actions:
     * 1. Disconnects from the BLE device.
     * 2. Dissociates from the BLE device, clearing any bonding information.
     * 3. Deletes the device's settings from the repository.
     */
    fun dissociate() {
        bleDevice.disconnect()
        bleDevice.dissociate()

        viewModelScope.launch {
            dataModule.deviceListSettingsRepository().deleteDevice(uiState.value.setting.address)
        }
    }

    init {
        // Connect to the Bluetooth device
        bleDevice.connect()

        // Read the device settings from the repository
        viewModelScope.launch {
            val deviceSetting = dataModule.deviceListSettingsRepository().getDevice(deviceAdd)
            deviceSetting?.let {
                _uiState.update { it ->
                    it.copy(
                        setting = DeviceSettingsItem(
                            name = deviceSetting.name,
                            address = deviceSetting.address,
                            currentRssi = null,
                            isOpened = deviceSetting.wasOpened,
                            isUnlocked = false,
                            thresholdNight = 50f,
                            currentBrightness = null,
                            autoUnlockEnabled = deviceSetting.autoUnlockEnabled,
                            autoUnlockRssiTh = deviceSetting.autoUnlockRssiTh,
                            connectionErrorMessage = null
                        )
                    )
                }
            }
        }

        // Manage connection timeout
        connectionStateJob = viewModelScope.launch {
            delay(5000)
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(
                    connectionErrorMessage = R.string.connection_error_timeout,
                    currentRssi = null))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleDevice.disconnect()
    }
}


class DeviceSettingViewModelFactory(
    private val dataModule: DataModule,
    private val deviceAdd: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceSettingsViewModel(dataModule, deviceAdd) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}