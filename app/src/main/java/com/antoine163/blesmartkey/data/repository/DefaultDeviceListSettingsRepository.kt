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
}