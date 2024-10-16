package com.antoine163.blesmartkey.model


/**
 * Data class representing the settings of a device.
 *
 * @property name The name of the device.
 * @property address The MAC address of the device.
 * @property currentRssi The current RSSI value of the device.
 * @property isOpened Indicates whether the device is currently opened.
 * @property isUnlocked Indicates whether the device is currently unlocked.
 * @property thresholdNight The brightness threshold for night mode.
 * @property currentBrightness The current brightness level.
 * @property autoUnlockEnabled Indicates whether auto-unlock is enabled.
 * @property autoUnlockRssiTh The RSSI threshold for auto-unlock.
 */
data class DeviceSetting(
    val name: String = "Unknown",
    val address: String = "",
    val currentRssi: Int? = null,
    val isOpened: Boolean = false,
    val isUnlocked: Boolean = false,
    val thresholdNight: Float = 50f,
    val currentBrightness: Float? = null,
    val autoUnlockEnabled: Boolean = false,
    val autoUnlockRssiTh: Int = -40
)
