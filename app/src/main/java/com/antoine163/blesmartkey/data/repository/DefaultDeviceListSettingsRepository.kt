package com.antoine163.blesmartkey.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import com.antoine163.blesmartkey.DeviceListSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * Default implementation of the [DeviceListSettingsRepository] interface.
 *
 * This repository uses a DataStore to persist and retrieve device list settings.
 *
 * @param dataStore The DataStore instance used to store the settings.
 */
class DefaultDeviceListSettingsRepository (
    private val dataStore: DataStore<DeviceListSettings>
) : DeviceListSettingsRepository {

    override val devices: Flow<DeviceListSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("BSK", "Error reading device list settings.", exception)
                emit(DeviceListSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }

    override suspend fun getDeviceListSettings(): DeviceListSettings {
        return devices.first()
    }

    override suspend fun updateDeviceListSettings(deviceListSettings: DeviceListSettings) {
        dataStore.updateData {
            deviceListSettings
        }
    }




//
//
//    override val devicesFlow: Flow<DevicesBleSettings> = context.devicesBleSettingsStore.data
//        .catch { exception ->
//            if (exception is IOException) {
//                Log.e("BSK", "Error reading settings.", exception)
//                emit(DevicesBleSettings.getDefaultInstance())
//            } else {
//                throw exception
//            }
//        }
//
//    override suspend fun getDevice(address: String): DeviceBleSettings? {
//        var device: DeviceBleSettings? = null
//
//        // Collect the devices from the repository and convert them to a list of DeviceListItem objects
//        devicesFlow.first { devicesBleSettings ->
//            // Find the device with the given address
//            device = devicesBleSettings.devicesList.find { it.address == address }
//            true
//        }
//
//        return device
//    }
//
//
//    override suspend fun getAllDevices(): DevicesBleSettings {
//        return devicesFlow.first()
//    }
//
//    override suspend fun updateDevice(device: DeviceBleSettings) {
//        context.devicesBleSettingsStore.updateData { currentDevices ->
//            val updatedDevices = currentDevices.devicesList
//            val index = updatedDevices.indexOfFirst { it.address == device.address }
//
//            if (index != -1) {
//                currentDevices.toBuilder().setDevices(index, device).build()
//            } else {
//                currentDevices.toBuilder().addDevices(device).build()
//            }
//        }
//    }
//
//    override suspend fun deleteDevice(address: String) {
//        context.devicesBleSettingsStore.updateData { currentDevices ->
//            val updatedDevices = currentDevices.devicesList
//            val index = updatedDevices.indexOfFirst { it.address == address }
//            currentDevices.toBuilder().removeDevices(index).build()
//        }
//    }
}