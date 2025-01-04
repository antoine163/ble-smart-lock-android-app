package com.antoine163.blesmartkey

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.antoine163.blesmartkey.data.DataModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AutoUnlockService : Service() {

    private val binder = LocalBinder()

    private val dataModule: DataModule by lazy { (application as BskApplication).dataModule() }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)



    companion object {
        private const val CHANNEL_ID = "auto_unlock_channel"
        private const val CHANNEL_NAME = "Déverrouillage automatique"
        private const val NOTIFICATION_ID = 1
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Service de déverrouillage automatique")
            .setContentText("En cours d'exécution en arrière-plan")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentIntent(pendingIntent)
            .build()
    }

    inner class LocalBinder : Binder() {
        fun getService(): AutoUnlockService = this@AutoUnlockService
    }

    fun start() {

        serviceScope.launch {
            val devices = dataModule.deviceListSettingsRepository().getDeviceList()
            Log.d("BSK", "AutoUnlockService::onStartCommand::serviceScope: devices: $devices")

            while(true) {
                Log.d("BSK", "AutoUnlockService is running...")
                delay(3000)
            }
        }

        Log.d("BSK", "AutoUnlockService::start")
    }

    fun stop() {
        Log.d("BSK", "AutoUnlockService::stop")
    }

    // onCreate ------------------------------------------------------------------------------------
    override fun onCreate() {
        super.onCreate()
        //createNotificationChannel()

        Log.d("BSK", "AutoUnlockService::onCreate")
    }

    // onStartCommand -----------------------------------------------------------------------------------

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Créer un canal de notification (pour Android 8.0 et supérieur)
        createNotificationChannel()

        // Créer la notification
        val notification = createNotification()

        // Démarrer le service au premier plan
        startForeground(NOTIFICATION_ID, notification)

        //Todo intent est null et que pas de devicer avec auto unlock activé fermet le service

        Log.d("BSK", "AutoUnlockService::onStartCommand")
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

        Log.d("BSK", "AutoUnlockService::onDestroy")
    }

//
//    // Bluetooth -----------------------------------------------------------------------------------
//
//    private val bluetoothManager: BluetoothManager? by lazy {
//        getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?
//
//    }
//
//    private val scanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            super.onScanResult(callbackType, result)
//            Log.d("BSK", "AutoUnlockService::Scan result: ${result.device.name} - ${result.device.address} - ${result.rssi}")
//        }
//
//        override fun onBatchScanResults(results: List<ScanResult?>?) {
//            super.onBatchScanResults(results)
//            Log.d("BSK", "AutoUnlockService::Batch scan results: ${results?.size} devices detected:")
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            super.onScanFailed(errorCode)
//            // Handle scan failure
//            Log.e("BSK", "AutoUnlockService::Scan failed with error code: $errorCode")
//        }
//    }
//
//    fun updateListScan() {
//
//        bluetoothManager?.adapter?.bluetoothLeScanner?.stopScan(scanCallback)
//
//        val scanFilters: MutableList<ScanFilter> = mutableListOf()
//        unlockDev.forEach {
//            val scanFilter = ScanFilter.Builder()
//                .setDeviceAddress(it.address)
//                .build()
//            scanFilters.add(scanFilter)
//        }
//
//        val scanSettings = ScanSettings.Builder()
//            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
//            .setReportDelay(0)
//            .build()
//        bluetoothManager?.adapter?.bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
//    }



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
