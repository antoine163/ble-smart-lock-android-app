package com.antoine163.blesmartkey.model

/**
 * A data class representing a device listed in the device list.
 *
 * @property name The name of the device.
 * @property address The address of the device.
 * @property rssi The RSSI value of the device, or null if not visible.
 * @property isDoorOpen A boolean indicating whether the device's door is open.
 */
data class DeviceListItem(
    val name: String,
    val address: String,
    val rssi: Int?,
    val isDoorOpen: Boolean
)
