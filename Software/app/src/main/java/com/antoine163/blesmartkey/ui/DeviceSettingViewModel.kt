package com.antoine163.blesmartkey.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.antoine163.blesmartkey.model.DeviceSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DeviceSettingUiState(
    val setting: DeviceSetting = DeviceSetting()
)

class DeviceSettingViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DeviceSettingUiState())
    val uiState: StateFlow<DeviceSettingUiState> = _uiState.asStateFlow()

    init {
        Log.d("BSK", "DeviceSettingViewModel init")
    }

    override fun onCleared() {
        super.onCleared()

        Log.d("BSK", "DeviceSettingViewModel onCleared")
    }

}