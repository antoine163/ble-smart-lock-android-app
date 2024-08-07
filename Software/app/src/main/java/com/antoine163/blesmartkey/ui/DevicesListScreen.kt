package com.antoine163.blesmartkey.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antoine163.blesmartkey.R
import com.antoine163.blesmartkey.model.DeviceListItem
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme

@Composable
fun DevicesListScreen(
    modifier: Modifier = Modifier,
    devices: List<DeviceListItem>,
    onSettingClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier,
    ) {
        itemsIndexed(devices) { index, device ->
            val topPadding = if (index == 0) dimensionResource(id = R.dimen.padding_small) else 0.dp
            DeviceListItemScreen(
                modifier = Modifier
                    .padding(
                        top = topPadding,
                        bottom = dimensionResource(id = R.dimen.padding_small))
                    .clickable(onClick = { onSettingClick(device.address) }),
                deviceName = device.name,
                isDoorOpen = device.isOpened,
                rssi = device.rssi,
                onOpenDoorClick = { /* TODO */ })
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    device = "id:S21 FE"
)
//@Preview(
//    name = "Light Mode",
//    device = "id:S21 FE"
//)
@Composable
private fun DevicesListScreenPreview() {
    // Create a dummy list of devices for previewing
    val devices = createDemoDeviceList()
    BleSmartKeyTheme {
        Surface {
            DevicesListScreen(
                devices = devices,
                onSettingClick = {})
        }
    }
}

fun createDemoDeviceList(): List<DeviceListItem> {
    // Create a dummy list of devices for previewing
    return listOf(
        DeviceListItem(
            name = "Device 1",
            address = "4D:74:99:36:BB:74",
            rssi = -55,
            isOpened = true
        ),
        DeviceListItem(
            name = "Device 2",
            address = "CD:EF:GH:IJ:KL:MN",
            rssi = -60,
            isOpened = false
        ),
        DeviceListItem(
            name = "Device 3",
            address = "OP:QR:ST:UV:WX:YZ",
            rssi = -70,
            isOpened = false
        ),
        DeviceListItem(
            name = "Device 4",
            address = "12:34:56:78:90:AB",
            rssi = null,
            isOpened = true
        ),
        DeviceListItem(
            name = "Device 5",
            address = "12:34:56:78:90:AB",
            rssi = null,
            isOpened = true
        ),
        DeviceListItem(
            name = "Device 6",
            address = "CD:EF:GH:IJ:KL:MN",
            rssi = null,
            isOpened = true
        ),
        DeviceListItem(
            name = "Device 7",
            address = "OP:QR:ST:UV:WX:YZ",
            rssi = null,
            isOpened = false
        ),
        DeviceListItem(
            name = "Device 8",
            address = "12:34:56:78:90:AB",
            rssi = null,
            isOpened = true
        )
    )
}
