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
    val devices: Flow<DeviceListSettings>

    /**
     * Retrieves the device list settings.
     *
     * This function fetches the current settings for the device list,
     * such as preferred sorting order, filtering criteria, etc.
     *
     * @return A [DeviceListSettings] object containing the current settings.
     */
    suspend fun getDeviceListSettings(): DeviceListSettings

    /**
     * Updates the device list settings.
     *
     * This function updates the stored device list settings with the provided [deviceListSettings].
     *
     * @param deviceListSettings The new device list settings to be applied.
     */
    suspend fun updateDeviceListSettings(deviceListSettings: DeviceListSettings)
}






