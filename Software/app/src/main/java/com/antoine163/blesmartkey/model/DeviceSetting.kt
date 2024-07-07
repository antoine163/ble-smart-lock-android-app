package com.antoine163.blesmartkey.model




/**
 * Represents a device setting.
 *
 * @property name The name of the device.
 * @property address The address of the device.
 * @property rssi The received signal strength indicator (RSSI) of the device, or `null` if not available.
 * @property isOpened Whether the device is currently open.
 * @property isUnlocked Whether the device is currently unlocked.
 * @property thresholdNight The light threshold for night mode.
 * @property currentBrightness The current brightness of the device.
 * @property autoUnlockEnable Whether auto unlock is enabled.
 * @property autoUnlockDistance The distance for auto unlock.
 * @property txPower The transmission power of the device.
 */
data class DeviceSetting(
    val name: String,
    val address: String,
    val rssi: Int?,
    val isOpened: Boolean,
    val isUnlocked: Boolean,
    val thresholdNight: Float,
    val currentBrightness: Float,
    val autoUnlockEnable: Boolean,
    val autoUnlockDistance: Float,
    val txPower: Int
)
