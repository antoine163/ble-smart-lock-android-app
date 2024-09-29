package com.antoine163.blesmartkey.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antoine163.blesmartkey.data.DevicesBleSettingsRepository
import com.antoine163.blesmartkey.model.DeviceListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DevicesListUiState(
    val devices: List<DeviceListItem> = listOf()
)

class DevicesListViewModel(
    devicesBleSettingsRepository : DevicesBleSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesListUiState())
    val uiState: StateFlow<DevicesListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Collect the devices from the repository and convert them to a list of DeviceListItem objects
            devicesBleSettingsRepository.devicesFlow.collect { devicesBleSettings ->

                // Convert the DevicesBleSettings object to a list of DeviceListItem objects
                val devices = devicesBleSettings.devicesList.map { deviceBleSettings ->
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
            }
        }
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
    private val devicesBleSettingsRepository : DevicesBleSettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DevicesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DevicesListViewModel(devicesBleSettingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}