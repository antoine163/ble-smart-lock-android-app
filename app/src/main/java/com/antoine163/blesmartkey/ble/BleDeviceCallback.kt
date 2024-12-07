package com.antoine163.blesmartkey.ble

open class BleDeviceCallback {
    open fun onConnectionStateChanged(isConnected: Boolean) {}
    open fun onConnectionFailed() {}

    open fun onLockStateChanged(isLocked: Boolean) {}
    open fun onDoorStateChanged(isOpened: Boolean) {}

    open fun onBrightnessRead(brightness: Float) {}
    open fun onBrightnessThChanged(brightness: Float) {}

    open fun onDeviceNameChanged(deviceName: String) {}
    open fun onRssiRead(rssi: Int) {}
}
