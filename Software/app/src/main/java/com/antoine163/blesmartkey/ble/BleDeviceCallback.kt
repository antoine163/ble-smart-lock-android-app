package com.antoine163.blesmartkey.ble

open class BleDeviceCallback {
    open fun onConnectionStateChanged(isConnected: Boolean) {}
    open fun onDoorStateChanged(isOpened: Boolean) {}
}
