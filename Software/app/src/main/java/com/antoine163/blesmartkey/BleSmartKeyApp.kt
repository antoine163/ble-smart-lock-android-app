package com.antoine163.blesmartkey

import android.app.Application
import com.antoine163.blesmartkey.data.AppContainer
import com.antoine163.blesmartkey.data.DefaultAppContainer

/**
 * Main Application class for the BleSmartKey app.
 *
 * Initializes the application and sets up the dependency container
 * using [DefaultAppContainer].
 */
class BleSmartKeyApp : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}