package com.antoine163.blesmartkey.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.UUID
import kotlin.coroutines.resume

class BleDevice(
    private val application: Application,
    private val address: String
) {

    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val device = bluetoothManager.adapter.getRemoteDevice(address)

    private var deviceGatt: BluetoothGatt? = null
    private val SERV_UUID = UUID.fromString("44707b20-3459-11ee-aea4-0800200c9a66")
    private val CHAR_UUID_LOCK_STATE = UUID.fromString("44707b21-3459-11ee-aea4-0800200c9a66")
    private val CHAR_UUID_DOOR_STATE = UUID.fromString("44707b22-3459-11ee-aea4-0800200c9a66")
    private val CHAR_UUID_OPEN_DOOR = UUID.fromString("44707b23-3459-11ee-aea4-0800200c9a66")
    private val CHAR_UUID_BRIGHTNESS = UUID.fromString("44707b24-3459-11ee-aea4-0800200c9a66")
    private val CHAR_UUID_BRIGHTNESS_TH = UUID.fromString("44707b25-3459-11ee-aea4-0800200c9a66")


    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            deviceGatt = gatt

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d("BSK", "Connected to device $address")
                    if (gatt?.discoverServices() == true) {
                        Log.d("BSK", "Discover services for device $address")
                    } else {
                        Log.e("BSK", "Discover services failed for device $address!")
                        /* todo manage error */
                    }
                } else {
                    Log.d("BSK", "Disconnected from device $address!")
                }
            } else {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.e("BSK", "Connection failed for device $address!")
                } else {
                    Log.e("BSK", "Disconnection failed for device $address!")
                }
                /* todo manage error */
            }
        }
//
//        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//            super.onServicesDiscovered(gatt, status)
//
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.d("BSK", "Services discovered for device $address")
//                // Process discovered services
//            } else {
//                Log.e("BSK", "Service discovery failed for device $address! Status: $status")
//                // Handle the error
//            }
//        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @SuppressLint("MissingPermission")
        fun unlock() {
            if (deviceGatt != null) {
                val service = deviceGatt!!.getService(SERV_UUID)
                val charUnlock = service?.getCharacteristic(CHAR_UUID_LOCK_STATE)

                if (charUnlock != null) {
                    val value = byteArrayOf(0x01)
                    deviceGatt?.writeCharacteristic(
                        charUnlock, value,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    )
                }
            }
        }


    }

    @SuppressLint("MissingPermission")
    private val gatt = device.connectGatt(application, false, gattCallback)
}