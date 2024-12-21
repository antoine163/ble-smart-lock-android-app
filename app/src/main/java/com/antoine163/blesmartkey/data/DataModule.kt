package com.antoine163.blesmartkey.data

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.antoine163.blesmartkey.data.datastore.DeviceListSettingsSerializer
import com.antoine163.blesmartkey.data.repository.DefaultDeviceListSettingsRepository

/**
 * A data module that provides access to Bluetooth and device list settings repositories.
 *
 * This class encapsulates the initialization and access to:
 * - BluetoothManager: For interacting with Bluetooth functionality.
 * - DeviceListSettingsRepository: For managing device list settings persisted using DataStore.
 *
 * @param application The Android application instance.
 */
class DataModule(application: Application) {

    // TODO a supprimer
    val context = application.applicationContext

    private val bluetoothManager: BluetoothManager =
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    /**
     * Provides access to the system's Bluetooth Manager.
     *
     * This function returns the system's Bluetooth Manager, which can be used to
     * perform Bluetooth operations such as scanning for devices, connecting to
     * devices, and managing Bluetooth profiles.
     *
     * @return The system's Bluetooth Manager.
     */
    fun bluetoothManager() = bluetoothManager

    private val deviceListSettingsRepository: DefaultDeviceListSettingsRepository by lazy {
        DefaultDeviceListSettingsRepository(
            DataStoreFactory.create(
                produceFile = { application.dataStoreFile("DeviceListSettings.pb") },
                serializer = DeviceListSettingsSerializer
            )
        )
    }

    /**
     * Provides access to the [DeviceListSettingsRepository] instance.
     *
     * This function returns the pre-existing instance of the `deviceListSettingsRepository`,
     * which is responsible for managing and persisting settings related to the device list.
     *
     * @return The [DeviceListSettingsRepository] instance.
     */
    fun deviceListSettingsRepository() = deviceListSettingsRepository


}