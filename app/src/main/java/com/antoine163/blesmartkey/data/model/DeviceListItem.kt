package com.antoine163.blesmartkey.data.model


/**
 * Data class representing a device item in a list.
 *
 * @property name The name of the device.
 * @property address The address of the device.
 * @property rssi The received signal strength indicator (RSSI) of the device,
 * or `null` if not available.
 * @property isOpened Whether the device is currently opened.
 */
data class DeviceListItem(
    val name: String,
    val address: String,
    val rssi: Int?,
    val isOpened: Boolean
)
