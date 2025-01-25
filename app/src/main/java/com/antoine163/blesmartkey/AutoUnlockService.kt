package com.antoine163.blesmartkey

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.antoine163.blesmartkey.ble.BleDevice
import com.antoine163.blesmartkey.ble.BleDeviceCallback
import com.antoine163.blesmartkey.data.DataModule
import com.antoine163.blesmartkey.data.model.DeviceAutoUnlok
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class AutoUnlockService : Service() {

    private val binder = LocalBinder()
    private val dataModule: DataModule by lazy { (application as BskApplication).dataModule() }
    private val bluetoothLeScanner: BluetoothLeScanner by lazy { dataModule.bluetoothManager().adapter.bluetoothLeScanner }
    private var autoUnlockJob: Job? = null
    private var unlockDeviceList = listOf<DeviceAutoUnlok>()


    companion object {
        private const val CHANNEL_ID = "auto_unlock_channel"
        private const val CHANNEL_NAME = "Automatic unlocking"
        private const val NOTIFICATION_ID = 1
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Automatic Unlock Service").setContentText("Running in background")
            .setSmallIcon(R.drawable.notification_icon).setContentIntent(pendingIntent).build()
    }

    inner class LocalBinder : Binder() {
        fun getService(): AutoUnlockService = this@AutoUnlockService
    }

    fun start() {
        if (autoUnlockJob == null) {
            autoUnlockJob = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {

                // Create a list of ScanFilter objects for each device to unlock
                val scanFilters: MutableList<ScanFilter> = mutableListOf()

                // Collect the latest device list settings and update unlockDeviceList
                dataModule.deviceListSettingsRepository().deviceListSettingsFlow.first { deviceListSettings ->
                    unlockDeviceList = deviceListSettings.devicesList
                        .filter { it.autoUnlockEnabled }
                        .map {
                            // Create a ScanFilter for each device to unlock
                            val scanFilter = ScanFilter.Builder()
                                .setDeviceAddress(it.address)
                                .build()
                            scanFilters.add(scanFilter)

                            DeviceAutoUnlok(
                                bleDevice = BleDevice(dataModule.context, it.address, bleDeviceCallback),
                                rssiToUnlock = it.autoUnlockRssiTh
                            )
                        }
                    true
                }

                // No device to unlock ?
                if (unlockDeviceList.isEmpty()) {
                    // Stop the service
                    stopSelf()
                } else {
                    // Scanning BLE unlock device list
                    bluetoothLeScanner.stopScan(scanCallback)
                    if (scanFilters.isNotEmpty()) {
                        val scanSettings = ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build()
                        bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
                    }
                }
            }
        } else {
            Log.w("BSK", "AutoUnlockService::start - Already running")
        }
    }

    fun stop() {
        autoUnlockJob?.cancel()
        autoUnlockJob = null

        bluetoothLeScanner.stopScan(scanCallback)
        unlockDeviceList.forEach {
            it.bleDevice.disconnect()
        }
        unlockDeviceList = listOf()

    }


    // onCreate ------------------------------------------------------------------------------------
    override fun onCreate() {
        super.onCreate()
    }

    // onStartCommand -----------------------------------------------------------------------------------
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Create notification channel (for Android 8.0 and above)
        createNotificationChannel()

        // Create the notification
        val notification = createNotification()

        // Start service in foreground
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    // Binder --------------------------------------------------------------------------------------
    override fun onBind(intent: Intent?): IBinder? {
        Log.d("BSK", "AutoUnlockService::onBind")
        return binder
    }

    // onDestroy -----------------------------------------------------------------------------------
    override fun onDestroy() {
        super.onDestroy()
        stop()
    }


    // Bluetooth -----------------------------------------------------------------------------------
    /**
     * Callback object for handling BLE device events.
     */
    private val bleDeviceCallback = object : BleDeviceCallback() {

        // Handle connection state changes
        override fun onConnectionStateChanged(bleDevice: BleDevice, isConnected: Boolean) {
            if (isConnected) {
                // Read Door state
                bleDevice.readDoorState()

                // Unlock the ble device
                bleDevice.unlock()
            }
        }

        override fun onConnectionStateFailed(bleDevice: BleDevice, status: Int) {
            Log.d("BSK", "Connection state failed for ${bleDevice.getAddress()}")
            bleDevice.disconnect()
        }

        // Handle door state changes
        override fun onDoorStateChanged(bleDevice: BleDevice, isOpened: Boolean) {
            // Update the repository if the door status has changed
            CoroutineScope(Dispatchers.IO).launch {

                // Update the device list settings
                dataModule.deviceListSettingsRepository()
                    .getDevice(bleDevice.getAddress())?.let { deviceSetting ->
                        if (deviceSetting.wasOpened != isOpened) {
                            dataModule.deviceListSettingsRepository()
                                .updateDevice(deviceSetting.copy { this.wasOpened = isOpened })
                        }
                    }
            }

            // Enable auto disconnect if the door is close
            if (isOpened == false)
            {
                unlockDeviceList.find { it.bleDevice == bleDevice }?.let { it ->
                    bleDevice.autoDisconnect((it.rssiToUnlock * 1.3f).toInt()) // add 30% offset
                }
            } else {
                bleDevice.autoDisconnectDisable()
            }
        }
    }

    /**
     * Scan callback object that handles the results of Bluetooth LE scans.
     */
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

//            // Extract device door state from advertising data
//            val isDoorOpened =
//                result.scanRecord?.advertisingDataMap?.get(0x2D)
//                    ?.takeIf { it.size >= 3 }
//                    ?.let { it[2].toInt() == 0x01 } == true

            Log.d(
                "BSK",
                "${result.device.address} -> Scanned : '${result.rssi}dbm"
            )

            // Find the device in the unlock list to start the unlock
            unlockDeviceList.find { it.bleDevice.getAddress() == result.device.address }?.let { it ->
                if (result.rssi >= it.rssiToUnlock) {
                    it.bleDevice.connect()
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Handle scan failure
            Log.e("BSK", "Scan failed with error code: $errorCode")
        }
    }
}
