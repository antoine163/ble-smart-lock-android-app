package com.antoine163.blesmartkey

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.antoine163.blesmartkey.data.DataModule
import kotlinx.coroutines.launch

/**
 * The main application class for the BSK application.
 *
 * This class is responsible for initializing the application, managing the data module,
 * and loading/saving device list settings.
 */
class BskApplication : Application() {

    fun dataModule() = dataModule
    private val dataModule: DataModule by lazy { DataModule(this) }


    override fun onCreate() {
        super.onCreate()

        // Load device list settings from data store
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            dataModule.deviceListSettingsRepository().load()
        }

        // Start the AutoUnlockService
        val serviceIntent = Intent(this, AutoUnlockService::class.java)
        startForegroundService(serviceIntent)
    }
}