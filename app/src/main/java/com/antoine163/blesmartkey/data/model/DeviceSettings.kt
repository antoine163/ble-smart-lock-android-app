package com.antoine163.blesmartkey.data.model


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
data class DeviceSettingsItem(
    val name: String,
    val address: String,
    val currentRssi: Int?,
    val isOpened: Boolean,
    val isUnlocked: Boolean,
    val thresholdNight: Float,
    val currentBrightness: Float?,
    val autoUnlockEnabled: Boolean,
    val autoUnlockRssiTh: Int,
    val connectionErrorMessage: Int?
)
