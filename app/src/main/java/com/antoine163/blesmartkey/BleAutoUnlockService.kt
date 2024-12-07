package com.antoine163.blesmartkey

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.antoine163.blesmartkey.model.DeviceAutoUnlok

class BleAutoUnlockService : Service() {

    private var unlockDev = mutableListOf<DeviceAutoUnlok>()

    // Binder --------------------------------------------------------------------------------------
    private val binder = BleAutoUnlockServiceBinder()
    inner class BleAutoUnlockServiceBinder : Binder() {
        fun updateUnlockDev(newUnlockDev: List<DeviceAutoUnlok>) {
            unlockDev = newUnlockDev.toMutableList()
            updateListScan();
            Log.d("BSK", "BleAutoUnlockService::bleDevList: $unlockDev")
        }

        fun getUnlockDev() : List<DeviceAutoUnlok> {
            Log.d("BSK", "BleAutoUnlockService::getUnlockDev: $unlockDev")
            return unlockDev
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("BSK", "BleAutoUnlockService::onBind")
        return binder
    }

    // onCreate ------------------------------------------------------------------------------------
    companion object {
        const val CHANNEL_ID = "ble_auto_unlock_service_channel"
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "TODO Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TODO Service de Premier Plan")
            .setContentText("TODO Le service est en cours d'exécution")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d("BSK", "BleAutoUnlockService::onCreate")
    }

    // onDestroy -----------------------------------------------------------------------------------

    override fun onDestroy() {
        super.onDestroy()

        Log.d("BSK", "BleAutoUnlockService::onDestroy")
    }

    // onStartCommand -----------------------------------------------------------------------------------

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        Log.d("BSK", "onStartCommand")
        return START_STICKY
    }


    // Bluetooth -----------------------------------------------------------------------------------

    private val bluetoothManager: BluetoothManager? by lazy {
        getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?

    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d("BSK", "BleAutoUnlockService::Scan result: ${result.device.name} - ${result.device.address} - ${result.rssi}")
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            super.onBatchScanResults(results)
            Log.d("BSK", "BleAutoUnlockService::Batch scan results: ${results?.size} devices detected:")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Handle scan failure
            Log.e("BSK", "BleAutoUnlockService::Scan failed with error code: $errorCode")
        }
    }

    fun updateListScan() {

        bluetoothManager?.adapter?.bluetoothLeScanner?.stopScan(scanCallback)

        val scanFilters: MutableList<ScanFilter> = mutableListOf()
        unlockDev.forEach {
            val scanFilter = ScanFilter.Builder()
                .setDeviceAddress(it.address)
                .build()
            scanFilters.add(scanFilter)
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setReportDelay(0)
            .build()
        bluetoothManager?.adapter?.bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
    }



//
//    // BleDeviceCallback instance to handle callbacks from the BleDevice
//    private val bleDeviceCallback = object : BleDeviceCallback() {
//
//        // Handle connection state changes
//        override fun onConnectionStateChanged(isConnected: Boolean) {
//            Log.d("BSK", "isConnected: $isConnected")
//        }
//
//        // Handle lock state changes
//        override fun onLockStateChanged(isLocked: Boolean) {
//            Log.d("BSK", "isLocked: $isLocked")
//        }
//
//        // Handle door state changes
//        override fun onDoorStateChanged(isOpened: Boolean) {
//            Log.d("BSK", "isOpened: $isOpened")
//        }
//
//        // Handle current brightness read
//        override fun onBrightnessRead(brightness: Float) {
//            Log.d("BSK", "brightness: $brightness")
//        }
//
//        // Handle brightness threshold read
//        override fun onBrightnessThChanged(brightness: Float) {
//            Log.d("BSK", "brightness: $brightness")
//        }
//
//        // Handle device name changes
//        override fun onDeviceNameChanged(deviceName: String) {
//            Log.d("BSK", "deviceName: $deviceName")
//        }
//
//        // Handle rssi changes
//        override fun onRssiRead(rssi: Int) {
//            Log.d("BSK", "rssi: $rssi")
//        }
//    }
//
//    // bleDevice instance to interact with the Bluetooth device
//    var bleDevice: BleDevice? = null
}
