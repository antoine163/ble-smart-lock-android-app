package com.antoine163.blesmartkey.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.antoine163.blesmartkey.ble.BleDevice
import com.antoine163.blesmartkey.ble.BleDeviceCallback
import com.antoine163.blesmartkey.data.DataModule
import com.antoine163.blesmartkey.data.model.DeviceSettingsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * UI state for representing the state of a device setting.
 * @property setting The device setting data. Defaults to an empty [DeviceSettingsItem].
 */
data class DeviceSettingsUiState(
    val setting: DeviceSettingsItem = DeviceSettingsItem()
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
    dataModule: DataModule,
    deviceAdd: String,
) : ViewModel() {

    // MutableStateFlow to hold the UI state of the device setting
    private val _uiState = MutableStateFlow(
        DeviceSettingsUiState(setting = DeviceSettingsItem(address = deviceAdd))
    )
    val uiState: StateFlow<DeviceSettingsUiState> = _uiState.asStateFlow()

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
     * Updates the device settings in the repository.
     *
     * This function launches a coroutine to update the device settings in the
     * [devicesBleSettingsRepository]. It maps the [DeviceSettingsItem] object to a
     * [DeviceBleSettings] object and updates the repository with the new settings.
     *
     * @param device The [DeviceSettingsItem] object containing the updated device settings.
     */
    private fun saveDeviceSetting(device: DeviceSettingsItem) {
//        viewModelScope.launch {
//            devicesBleSettingsRepository.updateDevice(
//                DeviceBleSettings.newBuilder()
//                    .setName(device.name)
//                    .setAddress(device.address)
//                    .setWasOpened(device.isOpened)
//                    .setAutoUnlockEnabled(device.autoUnlockEnabled)
//                    .setAutoUnlockRssiTh(device.autoUnlockRssiTh)
//                    .build()
//            )
//        }
    }

    // BleDeviceCallback instance to handle callbacks from the BleDevice
    private val bleDeviceCallback = object : BleDeviceCallback() {

        // Handle connection state changes
        override fun onConnectionStateChanged(isConnected: Boolean) {
            super.onConnectionStateChanged(isConnected)

            if (!isConnected) {
                _uiState.update { currentState ->
                    currentState.copy(setting = currentState.setting.copy(currentRssi = null))
                }
            } else {
                bleDevice.autoUnlock(-40)
            }
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }

        // Handle lock state changes
        override fun onLockStateChanged(isLocked: Boolean) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(isUnlocked = !isLocked))
            }
        }

        // Handle door state changes
        override fun onDoorStateChanged(isOpened: Boolean) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(isOpened = isOpened))
            }

            // Update the device setting in the repository
            saveDeviceSetting(_uiState.value.setting)
        }

        // Handle current brightness read
        override fun onBrightnessRead(brightness: Float) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(currentBrightness = brightness))
            }
        }

        // Handle brightness threshold read
        override fun onBrightnessThChanged(brightness: Float) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(thresholdNight = brightness))
            }

            // Update the device setting in the repository
            saveDeviceSetting(_uiState.value.setting)
        }

        // Handle device name changes
        override fun onDeviceNameChanged(deviceName: String) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(name = deviceName))
            }

            // Update the device setting in the repository
            saveDeviceSetting(_uiState.value.setting)
        }

        // Handle rssi changes
        override fun onRssiRead(rssi: Int) {
            _uiState.update { currentState ->
                currentState.copy(
                    setting = currentState.setting.copy(
                        currentRssi = rssi
                    )
                )
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

//        viewModelScope.launch {
//            devicesBleSettingsRepository.deleteDevice(uiState.value.setting.address)
//        }
    }

    init {
        // Connect to the Bluetooth device
        bleDevice.connect()

        // Read the device settings from the repository
//        viewModelScope.launch {
//            val device = devicesBleSettingsRepository.getDevice(deviceAdd)
//            device?.let {
//                _uiState.update { it ->
//                    it.copy(
//                        setting = DeviceSetting(
//                            name = device.name,
//                            address = device.address,
//                            isOpened = device.wasOpened,
//                            autoUnlockEnabled = device.autoUnlockEnabled,
//                            autoUnlockRssiTh = device.autoUnlockRssiTh
//                        )
//                    )
//                }
//            }
//        }
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