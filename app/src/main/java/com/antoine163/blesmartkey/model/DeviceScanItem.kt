package com.antoine163.blesmartkey.model

/**
 * Represents a single device found during a device scan.
 *
 * @property name The name of the device.
 * @property address The address of the device.
 * @property rssi The received signal strength indication (RSSI) of the device, in decibel-milliwatts (dBm).
 * Can be null if the RSSI is not available.
 */
data class DeviceScanItem(
    val name: String,
    val address: String,
    val rssi: Int?
)

