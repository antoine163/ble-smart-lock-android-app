package com.antoine163.blesmartkey.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antoine163.blesmartkey.ble.BleDevice
import com.antoine163.blesmartkey.ble.BleDeviceCallback
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
    deviceAdd: String
) : AndroidViewModel(application) {

    // MutableStateFlow to hold the UI state of the device setting
    private val _uiState = MutableStateFlow(
        DeviceSettingUiState(setting = DeviceSetting(address = deviceAdd)))
    val uiState: StateFlow<DeviceSettingUiState> = _uiState.asStateFlow()

    // BleDeviceCallback instance to handle callbacks from the BleDevice
    private val bleDeviceCallback = object : BleDeviceCallback() {

        // Handle connection state changes
        override fun onConnectionStateChanged(isConnected: Boolean) {
            super.onConnectionStateChanged(isConnected)

            if (!isConnected) {
                _uiState.update { currentState ->
                    currentState.copy(setting = currentState.setting.copy(rssi = null))
                }
            }
        }

        override fun onUnlock() {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(isUnlocked = true))
            }
        }

        // Handle door state changes
        override fun onDoorStateChanged(isOpened: Boolean) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(isOpened = isOpened))
            }
        }

        // Handle rssi changes
        override fun onRssiChanged(rssi: Int) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(rssi = rssi))
            }
        }

        // Handle device name changes
        override fun onDeviceNameChanged(deviceName: String) {
            _uiState.update { currentState ->
                currentState.copy(setting = currentState.setting.copy(name = deviceName))
            }
        }
    }

    // bleDevice instance to interact with the Bluetooth device
    val bleDevice: BleDevice = BleDevice(application, deviceAdd, bleDeviceCallback)

    init {
        viewModelScope.launch {
            while (true) {
                delay(800) // Wait for 800ms
                bleDevice.readRssi()
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
    private val deviceAdd: String
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceSettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceSettingViewModel(application, deviceAdd) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}