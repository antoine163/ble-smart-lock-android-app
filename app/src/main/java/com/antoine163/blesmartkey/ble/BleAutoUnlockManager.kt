package com.antoine163.blesmartkey.ble

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.antoine163.blesmartkey.BleAutoUnlockService
import com.antoine163.blesmartkey.BleAutoUnlockService.BleAutoUnlockServiceBinder
import com.antoine163.blesmartkey.model.DeviceAutoUnlok

class BleAutoUnlockManager(
    private val context: Context
) {
    private var unlockDevPending: MutableList<DeviceAutoUnlok>? = null

    private var serviceBinder: BleAutoUnlockServiceBinder? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceBinder = service as BleAutoUnlockServiceBinder
            Log.d("BSK", "BleAutoUnlockManager::connection::onServiceConnected")

            unlockDevPending?.let { unlockDev ->
                setUnlockDev(unlockDev)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("BSK", "BleAutoUnlockManager::connection::onServiceDisconnected")
            serviceBinder = null
        }
    }

    init {
        Log.d("BSK", "BleAutoUnlockManager::init")
        Intent(context, BleAutoUnlockService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }


    fun setUnlockDev(newUnlockDev: List<DeviceAutoUnlok>) {
        serviceBinder?.let { service ->
            unlockDevPending = null
            service.updateUnlockDev(newUnlockDev.toMutableList())

            if (newUnlockDev.isEmpty()) {
                servStop()
            } else {
                servStart()
            }
        } ?: run {
            unlockDevPending = newUnlockDev.toMutableList()
        }
    }

//    private fun servIsRunning(): Boolean {
//        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
//            if (BleAutoUnlockService::class.java.name == service.service.className) {
//                return true
//            }
//        }
//
//        return false
//    }

    private fun servStart() {
        Intent(context, BleAutoUnlockService::class.java).also { intent ->
            context.startForegroundService(intent)
        }
    }

    private fun servStop() {
        Intent(context, BleAutoUnlockService::class.java).also { intent ->
            context.stopService(intent)
        }
    }
}