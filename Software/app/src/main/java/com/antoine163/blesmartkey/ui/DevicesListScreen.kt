package com.antoine163.blesmartkey.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.antoine163.blesmartkey.R
import com.antoine163.blesmartkey.model.DeviceListItem

@Composable
fun DevicesListScreen(modifier: Modifier = Modifier,
                      devices: List<DeviceListItem>) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(devices) { index, device ->
            DeviceListItemScreen(
                modifier = if (index == 0) Modifier else Modifier.padding( top = dimensionResource(id = R.dimen.padding_small)),
                deviceName = device.name,
                isDoorOpen = device.isDoorOpen,
                rssi = device.rssi,
                onOpenDoorClick = {})
        }
    }
}



@Preview
@Composable
private fun DevicesListScreenPreview() {
    // Create a dummy list of devices for previewing
    val devices = listOf(
        DeviceListItem(
            name = "Device 1",
            address = "12:34:56:78:90:AB",
            rssi = -55,
            isDoorOpen = true
        ),
        DeviceListItem(
            name = "Device 2",
            address = "CD:EF:GH:IJ:KL:MN",
            rssi = -60,
            isDoorOpen = false
        ),
        DeviceListItem(
            name = "Device 3",
            address = "OP:QR:ST:UV:WX:YZ",
            rssi = -70,
            isDoorOpen = true
        ),
        DeviceListItem(
            name = "Device 4",
            address = "12:34:56:78:90:AB",
            rssi = null,
            isDoorOpen = true)
    )

    DevicesListScreen(devices = devices)
}
