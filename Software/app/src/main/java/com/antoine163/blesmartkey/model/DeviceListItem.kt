package com.antoine163.blesmartkey.model

data class DeviceListItem(
    val name: String,
    val address: String,
    val rssi: Int?,
    val isDoorOpen: Boolean
)
