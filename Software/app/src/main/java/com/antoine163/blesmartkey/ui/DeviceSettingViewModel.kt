package com.antoine163.blesmartkey.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.antoine163.blesmartkey.ble.BleDevice
import com.antoine163.blesmartkey.model.DeviceSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DeviceSettingUiState(
    val setting: DeviceSetting = DeviceSetting()
)

class DeviceSettingViewModel(
    application: Application,
    deviceAdd: String
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        DeviceSettingUiState(setting = DeviceSetting(address = deviceAdd)))
    val uiState: StateFlow<DeviceSettingUiState> = _uiState.asStateFlow()

    private val bleDevice = BleDevice(application, deviceAdd)

    init {
        Log.d("BSK", "DeviceSettingViewModel init: ${uiState.value.setting.address}")

        _uiState.update { currentState ->
            currentState.copy(setting = DeviceSetting(rssi = -70))
        }
    }

    override fun onCleared() {
        super.onCleared()

        Log.d("BSK", "DeviceSettingViewModel onCleared")
    }
}

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