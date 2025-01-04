package com.antoine163.blesmartkey.ble

open class BleDeviceCallback {
    open fun onConnectionStateChanged(bleDevice: BleDevice, isConnected: Boolean) {}
    open fun onConnectionStateFailed(bleDevice: BleDevice) {}

    open fun onLockStateChanged(bleDevice: BleDevice, isLocked: Boolean) {}
    open fun onDoorStateChanged(bleDevice: BleDevice, isOpened: Boolean) {}

    open fun onBrightnessRead(bleDevice: BleDevice, brightness: Float) {}
    open fun onBrightnessThChanged(bleDevice: BleDevice, brightness: Float) {}

    open fun onDeviceNameChanged(bleDevice: BleDevice, deviceName: String) {}
    open fun onRssiRead(bleDevice: BleDevice, rssi: Int) {}
}
