package com.antoine163.blesmartkey.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antoine163.blesmartkey.DeviceBleSettings
import com.antoine163.blesmartkey.ble.BleDevice
import com.antoine163.blesmartkey.ble.BleDeviceCallback
import com.antoine163.blesmartkey.data.DevicesBleSettingsRepository
import com.antoine163.blesmartkey.model.DeviceSetting
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for representing the state of a device setting.
 * @property setting The device setting data. Defaults to an empty [DeviceSetting].
 */
data class DeviceSettingUiState(
    val setting: DeviceSetting = DeviceSetting()
)

/**
 * ViewModel for the Device Setting screen.
 *
 * This ViewModel is responsible for managing the UI state of the Device Setting screen
 * and interacting with the BleDevice class.
 *
 * @param application The application context.
 * @param deviceAdd The MAC address of the Bluetooth device.
 */
class DeviceSettingViewModel(
    application: Application,
    deviceAdd: String,
    val devicesBleSettingsRepository: DevicesBleSettingsRepository
) : AndroidViewModel(application) {

    // MutableStateFlow to hold the UI state of the device setting
    private val _uiState = MutableStateFlow(
        DeviceSettingUiState(setting = DeviceSetting(address = deviceAdd))
    )
    val uiState: StateFlow<DeviceSettingUiState> = _uiState.asStateFlow()

    /**
     * Enables or disables the auto-unlock feature.
     *
     * This function updates the UI state and saves the updated setting to persistent storage.
     *
     * @param enable `true` to enable auto-unlock, `false` to disable it.
     */
    fun autoUnlock(enable : Boolean) {
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
    fun setAutoUnlockRssiTh(rssiTh : Int) {
        _uiState.update { currentState ->
            currentState.copy(setting = currentState.setting.copy(autoUnlockRssiTh = rssiTh))
        }

        saveDeviceSetting(_uiState.value.setting)
    }

    /**
     * Updates the device settings in the repository.
     *
     * This function launches a coroutine to update the device settings in the
     * [devicesBleSettingsRepository]. It maps the [DeviceSetting] object to a
     * [DeviceBleSettings] object and updates the repository with the new settings.
     *
     * @param device The [DeviceSetting] object containing the updated device settings.
     */
    private fun saveDeviceSetting(device: DeviceSetting) {
        viewModelScope.launch {
            devicesBleSettingsRepository.updateDevice(
                DeviceBleSettings.newBuilder()
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
        override fun onConnectionStateChanged(isConnected: Boolean) {
            super.onConnectionStateChanged(isConnected)

            if (!isConnected) {
                _uiState.update { currentState ->
                    currentState.copy(setting = currentState.setting.copy(currentRssi = null))
                }
            }
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

    // bleDevice instance to interact with the Bluetooth device
    val bleDevice: BleDevice = BleDevice(getApplication<Application>(), deviceAdd, bleDeviceCallback)

    init {
        viewModelScope.launch {

            // Initialize the device setting with default values
            _uiState.update { it ->
                it.copy(setting = DeviceSetting(address = deviceAdd) )
            }

            // Read the device settings from the repository
            val device = devicesBleSettingsRepository.getDevice(deviceAdd)
            device?.let {
                _uiState.update { it ->
                    it.copy(
                        setting = DeviceSetting(
                            name = device.name,
                            address = device.address,
                            isOpened = device.wasOpened,
                            autoUnlockEnabled = device.autoUnlockEnabled,
                            autoUnlockRssiTh = device.autoUnlockRssiTh
                        )
                    )
                }
            }

            // Connect to the Bluetooth device
            bleDevice.connect()

            // Read Rssi and brightness every 0.8s
            while (true) {
                delay(800)
                bleDevice.readRssi()
                bleDevice.readBrightness()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleDevice.disconnect()
    }
}

/**
 * Factory for creating a [DeviceSettingViewModel].
 * This factory takes an [Application] and a device address as parameters, which are then used to create the ViewModel.
 * @param application The application context.
 * @param deviceAdd The Bluetooth device address.
 */
class DeviceSettingViewModelFactory(
    private val application: Application,
    private val devicesBleSettingsRepository: DevicesBleSettingsRepository,
    private val deviceAdd: String
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceSettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceSettingViewModel(application, deviceAdd, devicesBleSettingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}