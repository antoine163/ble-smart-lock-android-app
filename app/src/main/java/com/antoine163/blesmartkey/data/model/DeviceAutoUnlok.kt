package com.antoine163.blesmartkey.data.model

import com.antoine163.blesmartkey.ble.BleDevice

/**
 * Represents a device configured for automatic unlocking.
 *
 * This data class encapsulates the information needed to identify and interact with a
 * Bluetooth Low Energy (BLE) device that is intended to be automatically unlocked
 * when it's within a certain proximity.
 *
 * @property bleDevice The [BleDevice] associated with this auto-unlock configuration.
 *                     It contains the necessary details to identify and connect to the BLE peripheral.
 * @property rssiToUnlock The Received Signal Strength Indicator (RSSI) threshold for unlocking.
 *                        When the RSSI of the [bleDevice] exceeds (becomes more positive than) this value,
 *                        the automatic unlock process should be initiated.
 */
data class DeviceAutoUnlok(
    val bleDevice: BleDevice,
    val rssiToUnlock: Int
)