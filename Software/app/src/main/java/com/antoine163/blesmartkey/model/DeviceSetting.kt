package com.antoine163.blesmartkey.model



/**
 * Data class representing a device setting.
 *
 * @property name The name of the device.
 * @property address The address of the device.
 * @property rssi The received signal strength indicator (RSSI) of the device, in dBm. Can be null if not available.
 * @property isDoorOpen Whether the door is currently open.
 * @property isUnlock Whether the device is currently unlocked.
 * @property thresholdNight The brightness threshold for night mode.
 * @property currentBrightness The current brightness of the device.
 * @property autoUnlock Whether auto unlock is enabled.
 * @property autoUnlockDistance The distance threshold for auto unlock, in meters.
 * @property txPower The transmission power of the device, in dBm.
 */
data class DeviceSetting(
    val name: String,
    val address: String,
    val rssi: Int?,
    val isDoorOpen: Boolean,
    val isUnlock: Boolean,
    val thresholdNight: Float,
    val currentBrightness: Float,
    val autoUnlock: Boolean,
    val autoUnlockDistance: Float,
    val txPower: Int
)
