package com.antoine163.blesmartkey.data.model


/**
 * Data class representing a device item in a list.
 *
 * @property name The name of the device.
 * @property address The MAC address of the device.
 * @property rssi The Received Signal Strength Indicator (RSSI) of the device, which indicates the signal strength. Can be null.
 * @property isOpened Indicates whether the device is currently open/connected.
 * @property isOpening Indicates whether the device is in the process of opening/connecting.
 * @property isOpenTimeout Indicates whether a timeout occurred while trying to open the device.
 */
data class DeviceListItem(
    val name: String,
    val address: String,
    val rssi: Int?,
    val isOpened: Boolean,
    val isOpening: Boolean,
    val isOpenTimeout: Boolean
)
