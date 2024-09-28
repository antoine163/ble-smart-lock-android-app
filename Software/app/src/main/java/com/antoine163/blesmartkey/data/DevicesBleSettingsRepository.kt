package com.antoine163.blesmartkey.data

import androidx.datastore.core.DataStore
import com.antoine163.blesmartkey.DevicesBleSettings

class DevicesBleSettingsRepository (
    private val dataStore: DataStore<DevicesBleSettings>
){
    //...
}