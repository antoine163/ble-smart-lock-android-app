package com.antoine163.blesmartkey.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.antoine163.blesmartkey.DeviceBleSettings
import com.antoine163.blesmartkey.DevicesBleSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toCollection
import java.io.IOException

private val Context.devicesBleSettingsStore: DataStore<DevicesBleSettings> by dataStore(
    fileName = "DevicesBleSettings.pb",
    serializer = DevicesBleSettingsSerializer
)

interface DevicesBleSettingsRepository {
    val devicesFlow: Flow<DevicesBleSettings>
    suspend fun updateDevice(device: DeviceBleSettings)
}

class DevicesBleSettingsRepositoryApp(
    private val context: Context
) : DevicesBleSettingsRepository {

    override val devicesFlow: Flow<DevicesBleSettings> = context.devicesBleSettingsStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("BSK", "Error reading settings.", exception)
                emit(DevicesBleSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }

    override suspend fun updateDevice(device: DeviceBleSettings) {
        context.devicesBleSettingsStore.updateData { currentDevices ->
            val updatedDevices = currentDevices.devicesList
            val index = updatedDevices.indexOfFirst { it.address == device.address }

            if (index != -1) {
                currentDevices.toBuilder().setDevices(index, device).build()
            } else {
                currentDevices.toBuilder().addDevices(device).build()
            }
        }
    }
}