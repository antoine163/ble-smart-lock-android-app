package com.antoine163.blesmartkey.data

interface AppContainer {
    //val devicesBleSettingsRepository: DevicesBleSettingsRepository
}

class DefaultAppContainer : AppContainer {

//    private val dataStore: DataStore<DevicesBleSettings> by dataStore(
//        fileName = "DevicesBleSettings.pb",
//        serializer = DevicesBleSettingsSerializer
//    )


//    override val deviceBleSettingsRepository: DevicesBleSettingsRepository by lazy {
//        DevicesBleSettingsRepository(dataStore)
//    }
}