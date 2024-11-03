package com.antoine163.blesmartkey.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.antoine163.blesmartkey.DeviceBleSettings
import com.antoine163.blesmartkey.DevicesBleSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * DataStore instance for storing and retrieving Bluetooth Low Energy (BLE) settings for devices.
 *
 * This property provides access to the DataStore that manages the persistent storage of
 * BLE settings for devices. These settings are serialized using the `DevicesBleSettingsSerializer`
 * and stored in a file named "DevicesBleSettings.pb".
 *
 * The `Context` receiver allows for easy access to the application context within
 * the scope where this property is used.
 */
private val Context.devicesBleSettingsStore: DataStore<DevicesBleSettings> by dataStore(
    fileName = "DevicesBleSettings.pb",
    serializer = DevicesBleSettingsSerializer
)

/**
 * Repository interface for managing Bluetooth Low Energy (BLE) settings for devices.
 * This interface provides methods for accessing and updating BLE settings for connected devices.
 */
interface DevicesBleSettingsRepository {
    /**
     * A flow emitting the current [DevicesBleSettings].
     *
     * This flow will emit updates whenever the Bluetooth Low Energy device settings change.
     * For example, when a device is connected, disconnected, or its connection parameters are modified.
     *
     * Observers can collect from this flow to be notified of changes and react accordingly.
     */
    val devicesFlow: Flow<DevicesBleSettings>

    /**
     * Retrieves the Bluetooth device settings for a given device address.
     *
     * This function attempts to retrieve the stored Bluetooth device settings
     * associated with the provided device address.
     *
     * @param address The Bluetooth address of the device.
     * @return The [DeviceBleSettings] object containing the device settings if found,
     *         otherwise null if the device settings are not stored or an error occurs.
     *
     * @suspend This function is suspendable and should be called from a coroutine.
     */
    suspend fun getDevice(address: String) : DeviceBleSettings?

    /**
     * Updates the settings of a Bluetooth Low Energy (BLE) device.
     *
     * This function takes a [DeviceBleSettings] object containing the updated settings
     * and applies them to the corresponding BLE device.
     *
     * @param device The [DeviceBleSettings] object containing the updated settings for the BLE device.
     *
     * @suspend This function is suspendable and should be called from a coroutine.
     */
    suspend fun updateDevice(device: DeviceBleSettings)
}

/**
 * An implementation of the [DevicesBleSettingsRepository] interface that uses DataStore
 * to persist and retrieve Bluetooth device settings.
 *
 * This repository provides access to the list of Bluetooth devices and their settings,
 * allowing for retrieval, updating, and observing changes.
 *
 * @param context The Android context used to access DataStore.
 */
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

    override suspend fun getDevice(address: String): DeviceBleSettings? {
        var device : DeviceBleSettings? = null

        // Collect the devices from the repository and convert them to a list of DeviceListItem objects
        devicesFlow.first { devicesBleSettings ->
            // Find the device with the given address
            device = devicesBleSettings.devicesList.find {it.address == address}
            true
        }

        return device;
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