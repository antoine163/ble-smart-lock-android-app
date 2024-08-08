package com.antoine163.blesmartkey.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import java.util.UUID

@SuppressLint("MissingPermission")
/* todo deconter le device quand BleDevice est détruit */
class BleDevice(
    private val application: Application,
    address: String,
    callback: BleDeviceCallback
) {
    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(address)

    private var gattDevice: BluetoothGatt? = null
    private var gattCharDeviceName: BluetoothGattCharacteristic? = null
    private var gattCharLockState: BluetoothGattCharacteristic? = null
    private var gattCharDoorState: BluetoothGattCharacteristic? = null
    private var gattCharOpenDoor: BluetoothGattCharacteristic? = null
    private var gattCharBrightness: BluetoothGattCharacteristic? = null
    private var gattCharBrightnessTh: BluetoothGattCharacteristic? = null


    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("BSK", "onConnectionStateChange: $status : $newState")
            super.onConnectionStateChange(gatt, status, newState)

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                callback.onConnectionStateChanged(true)
                // Attempts to discover services after successful connection.
                gatt?.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                callback.onConnectionStateChanged(false)
                disconnect()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BSK", "Services discovered for device $address")

                gatt?.let {
                    // Save the gatt device
                    gattDevice = gatt

                    // Get the generic access service and characteristic
                    val gattServiceGenericAccess = gatt.getService(SERV_UUID_GENERIC_ACCESS)
                    gattCharDeviceName = gattServiceGenericAccess?.getCharacteristic(CHAR_UUID_DEVICE_NAME)

                    // Get the application service and characteristics
                    val gattServiceApp = gatt.getService(SERV_UUID_APP)
                    gattCharLockState = gattServiceApp?.getCharacteristic(CHAR_UUID_LOCK_STATE)
                    gattCharDoorState = gattServiceApp?.getCharacteristic(CHAR_UUID_DOOR_STATE)
                    gattCharOpenDoor = gattServiceApp?.getCharacteristic(CHAR_UUID_OPEN_DOOR)
                    gattCharBrightness = gattServiceApp?.getCharacteristic(CHAR_UUID_BRIGHTNESS)
                    gattCharBrightnessTh = gattServiceApp?.getCharacteristic(CHAR_UUID_BRIGHTNESS_TH)

                    /* Todo manage error if service and characteristic not found */

                    // Enable notifications for the door state characteristic
                    gatt.setCharacteristicNotification(gattCharDoorState, true)

                    // Read the device name characteristic
                    gatt.readCharacteristic(gattCharDeviceName)
                }
            } else {
                Log.e("BSK", "Service discovery failed for device $address! Status: $status")
                // Handle the error
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)

            when (characteristic.uuid) {
                CHAR_UUID_DEVICE_NAME -> {
                    val deviceName = String(value)
                    callback.onDeviceNameChanged(deviceName)
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            when (characteristic?.uuid) {
                CHAR_UUID_LOCK_STATE -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        callback.onUnlock()
                    }
                }
                CHAR_UUID_DEVICE_NAME -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // Read the device name characteristic
                        gatt?.readCharacteristic(gattCharDeviceName)
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)

            // Handle the door state change
            when (characteristic.uuid) {
                CHAR_UUID_DOOR_STATE -> {
                    val isOpened = value[0] == 0x01.toByte()
                    callback.onDoorStateChanged(isOpened)
                }
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            // Handle the RSSI value
            callback.onRssiChanged(rssi)
        }
    }

    init {
        connect()
    }

    @SuppressLint("NewApi")
    fun setDeviceName(deviceName: String) {
        gattCharDeviceName?.let { charDeviceName ->
            gattDevice?.writeCharacteristic(
                charDeviceName, deviceName.toByteArray(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        }
    }

    @SuppressLint("NewApi")
    fun unlock() {
        gattCharLockState?.let { charLockState ->
            gattDevice?.writeCharacteristic(
                charLockState, byteArrayOf(0x01),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        }
    }

    @SuppressLint("NewApi")
    fun openDoor() {
        gattCharOpenDoor?.let { charOpenDoor ->
            gattDevice?.writeCharacteristic(
                charOpenDoor, byteArrayOf(0x01),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        }
    }

    fun readRssi() {
        gattDevice?.readRemoteRssi()
    }

    fun connect() {
        if (gattDevice == null) {
            bluetoothDevice.connectGatt(application, true, gattCallback)
        }
    }

    fun disconnect() {
        gattDevice?.disconnect()

        gattDevice = null
        gattCharDeviceName = null
        gattCharLockState = null
        gattCharDoorState = null
        gattCharOpenDoor = null
        gattCharBrightness = null
        gattCharBrightnessTh  = null
    }


    companion object {
        private val SERV_UUID_GENERIC_ACCESS = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
        private val CHAR_UUID_DEVICE_NAME = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
        private val SERV_UUID_APP = UUID.fromString("44707b20-3459-11ee-aea4-0800200c9a66")
        private val CHAR_UUID_LOCK_STATE = UUID.fromString("44707b21-3459-11ee-aea4-0800200c9a66")
        private val CHAR_UUID_DOOR_STATE = UUID.fromString("44707b22-3459-11ee-aea4-0800200c9a66")
        private val CHAR_UUID_OPEN_DOOR = UUID.fromString("44707b23-3459-11ee-aea4-0800200c9a66")
        private val CHAR_UUID_BRIGHTNESS = UUID.fromString("44707b24-3459-11ee-aea4-0800200c9a66")
        private val CHAR_UUID_BRIGHTNESS_TH = UUID.fromString("44707b25-3459-11ee-aea4-0800200c9a66")
    }
}