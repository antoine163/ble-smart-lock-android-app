package com.antoine163.blesmartkey.data.repository

import com.antoine163.blesmartkey.DeviceListSettings
import com.antoine163.blesmartkey.DeviceSettings
import kotlinx.coroutines.flow.Flow


/**
 * Repository interface for managing device list settings.
 *
 * This interface provides methods for accessing and updating the device list settings.
 */
interface DeviceListSettingsRepository {
    /**
     * A flow emitting the current list of devices and their settings.
     *
     * This flow will emit updates whenever the list of devices or their settings change.
     * For example, when a new device is added, removed, or its settings are modified.
     *
     * Consumers can collect from this flow to observe and react to changes in the device list.
     */
    val deviceListSettingsFlow: Flow<DeviceListSettings>

    suspend fun getDevice(address: String): DeviceSettings?
    suspend fun getDeviceList(): DeviceListSettings
    suspend fun updateDevice(device: DeviceSettings)
    suspend fun deleteDevice(address: String)

    suspend fun load()
    suspend fun save()
}






