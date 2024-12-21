package com.antoine163.blesmartkey.data.model

/**
 * Data class representing a device that can automatically unlock.
 *
 * @property address The Bluetooth address of the device.
 * @property rssiToUnlock The RSSI threshold at which the device should trigger the unlock action.
 */
data class DeviceAutoUnlok(
    val address: String,
    val rssiToUnlock: Int
)