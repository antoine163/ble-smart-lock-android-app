package com.antoine163.blesmartkey.data.model



/**
 * Data class representing the settings of a device.
 *
 * @param name The name of the device.
 * @param address The MAC address of the device.
 * @param currentRssi The current RSSI (Received Signal Strength Indicator) of the device, or null if not available.
 * @param isOpened Indicates whether the device is currently opened.
 * @param isUnlocked Indicates whether the device is currently unlocked.
 * @param thresholdNight The brightness threshold for night mode.
 * @param currentBrightness The current brightness of the device, or null if not available.
 * @param autoUnlockEnabled Indicates whether auto-unlock is enabled for the device.
 * @param autoUnlockRssiTh The RSSI threshold for auto-unlock.
 * @param connectionStateFailed The number of connection failures for the device.
 * Can be a value of BluetoothGatt :
 * - 0 [BluetoothGatt.GATT_SUCCESS]: No error
 * - 22: Pairing failed
 * - 147 [BluetoothGatt.GATT_CONNECTION_TIMEOUT]: Connection timeout.
 * - x: Unknown error
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
    val connectionStateFailed: Int
)
