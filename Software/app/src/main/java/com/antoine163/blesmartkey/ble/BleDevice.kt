package com.antoine163.blesmartkey.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

@SuppressLint("MissingPermission")
class BleDevice(
    private val application: Application,
    address: String,
    callback: BleDeviceCallback
) {
    private val bluetoothManager: BluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothDevice: BluetoothDevice = bluetoothManager.adapter.getRemoteDevice(address)

    private var gattDevice: BluetoothGatt? = null
    private var gattCharDeviceName: BluetoothGattCharacteristic? = null
    private var gattCharLockState: BluetoothGattCharacteristic? = null
    private var gattCharDoorState: BluetoothGattCharacteristic? = null
    private var gattCharOpenDoor: BluetoothGattCharacteristic? = null
    private var gattCharBrightness: BluetoothGattCharacteristic? = null
    private var gattCharBrightnessTh: BluetoothGattCharacteristic? = null

    private var readCharMap = mutableMapOf<UUID, BluetoothGattCharacteristic>()


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
                // Disconnected from the GATT Server
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

                    // Handle the door state change, before a connection the lock is locked
                    callback.onLockStateChanged( true )

                    // Read the device name characteristic
                    readCharacteristics(gattCharDeviceName)

                    // Read the door state characteristic
                    readCharacteristics(gattCharDoorState)

                    // Read the brightness threshold characteristic
                    readCharacteristics(gattCharBrightnessTh)
                }
            } else {
                Log.e("BSK", "Service discovery failed for device $address! Status: $status")
                // Handle the error
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
                        callback.onLockStateChanged( false )
                    }
                }
                CHAR_UUID_DEVICE_NAME -> {
                    readCharacteristics(gattCharDeviceName)
                }
                CHAR_UUID_BRIGHTNESS_TH -> {
                    readCharacteristics(gattCharBrightnessTh)
                }
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
                CHAR_UUID_DOOR_STATE -> {
                    val isOpened = value[0] == 0x01.toByte()
                    callback.onDoorStateChanged(isOpened)
                }
                CHAR_UUID_BRIGHTNESS -> {
                    val brightness =
                        ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getFloat()
                    callback.onBrightnessRead(brightness)
                }
                CHAR_UUID_BRIGHTNESS_TH -> {
                    val brightness =
                        ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getFloat()
                    callback.onBrightnessThChanged(brightness)
                }
            }

            readCharMap.remove(characteristic.uuid)
            readNextCharacteristic()
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
            callback.onRssiRead(rssi)
        }
    }

    private fun readCharacteristics(gattChar: BluetoothGattCharacteristic?) {
        if (gattChar != null) {
            if (readCharMap.isEmpty()) {
                readCharMap[gattChar.uuid] = gattChar
                readNextCharacteristic()
            } else {
                readCharMap.putIfAbsent(gattChar.uuid, gattChar)
            }
        }
    }

    private fun readNextCharacteristic() {
        if (!readCharMap.isEmpty()) {
            val char = readCharMap.values.first()
            gattDevice?.readCharacteristic(char)
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
    fun setBrightnessTh(brightnessTh: Float) {
        gattCharBrightnessTh?.let { charBrightnessTh ->

            val byteArray = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(brightnessTh)
                .array()

            gattDevice?.writeCharacteristic(
                charBrightnessTh, byteArray,
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

    fun readBrightness() {
        readCharacteristics(gattCharBrightness)
    }

    fun connect() {
        if (gattDevice == null) {
            bluetoothDevice.connectGatt(application, false, gattCallback)
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

        readCharMap.clear()
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