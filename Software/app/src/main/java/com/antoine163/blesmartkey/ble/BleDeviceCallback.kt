package com.antoine163.blesmartkey.ble

open class BleDeviceCallback {
    open fun onConnectionStateChanged(isConnected: Boolean) {}
    open fun onUnlock() {}
    open fun onDoorStateChanged(isOpened: Boolean) {}
    open fun onRssiChanged(rssi: Int) {}
    open fun onDeviceNameChanged(deviceName: String) {}
}
