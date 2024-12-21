package com.antoine163.blesmartkey.data.repository

import androidx.datastore.core.DataStore
import com.antoine163.blesmartkey.DeviceListSettings
import com.antoine163.blesmartkey.DeviceSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update


/**
 * Default implementation of the [DeviceListSettingsRepository] interface.
 * This class uses a DataStore to persist and retrieve device list settings.
 *
 * @param dataStore The DataStore instance used to store the device list settings.
 */
class DefaultDeviceListSettingsRepository (
    private val dataStore: DataStore<DeviceListSettings>
) : DeviceListSettingsRepository {

    override val deviceListSettingsFlow = MutableStateFlow(DeviceListSettings.getDefaultInstance())

    override suspend fun getDevice(address: String): DeviceSettings? {
        var device: DeviceSettings? = null

        // Collect the devices from the repository and convert them to a list of DeviceListItem objects
        deviceListSettingsFlow.first { devicesBleSettings ->
            // Find the device with the given address
            device = devicesBleSettings.devicesList.find { it.address == address }
            true
        }

        return device
    }


    override suspend fun getDeviceList(): DeviceListSettings {
        return deviceListSettingsFlow.first()
    }

    override suspend fun updateDevice(device: DeviceSettings) {

        deviceListSettingsFlow.update { currentDevices ->
            val updatedDevices = currentDevices.devicesList
            val index = updatedDevices.indexOfFirst { it.address == device.address }

            if (index != -1) {
                currentDevices.toBuilder().setDevices(index, device).build()
            } else {
                currentDevices.toBuilder().addDevices(device).build()
            }
        }
    }

    override suspend fun deleteDevice(address: String) {
        deviceListSettingsFlow.update { currentDevices ->
            val updatedDevices = currentDevices.devicesList
            val index = updatedDevices.indexOfFirst { it.address == address }
            currentDevices.toBuilder().removeDevices(index).build()
        }
    }


    override suspend fun load() {
        deviceListSettingsFlow.value = dataStore.data.first()
    }

    override suspend fun save() {
        dataStore.updateData { deviceListSettingsFlow.value }
    }
}