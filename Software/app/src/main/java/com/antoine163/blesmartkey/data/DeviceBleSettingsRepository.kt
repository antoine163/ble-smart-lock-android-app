package com.antoine163.blesmartkey.data

import androidx.datastore.core.DataStore
import com.antoine163.blesmartkey.DeviceBleSettings

class DeviceBleSettingsRepository (
    private val dataStore: DataStore<DeviceBleSettings>
){
    //...
}