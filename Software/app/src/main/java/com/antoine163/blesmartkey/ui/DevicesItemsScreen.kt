package com.antoine163.blesmartkey.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.antoine163.blesmartkey.R


@Composable
fun DeviceListItem(
    modifier: Modifier = Modifier,
    deviceName: String,
    isDoorOpen: Boolean,
    isVisible: Boolean,
    rssi: Int,
    onOpenDoorClick: () -> Unit,
){
    DeviceItem(
        modifier = modifier,
        deviceName = deviceName,
        rssi = rssi,
        isVisible = isVisible,
        infoText = if (isDoorOpen) stringResource(id = R.string.state_open) else stringResource(id = R.string.state_close),
        infoWarnings = !isVisible && isDoorOpen,
        buttonText = stringResource(id = R.string.open_door),
        buttonEnable = isVisible,
        onButtonClick = onOpenDoorClick
    )
}

@Composable
fun DeviceScanItem(
    modifier: Modifier = Modifier,
    deviceName: String,
    deviceAdd: String,
    isVisible: Boolean = true,
    rssi: Int,
    onConnectClick: () -> Unit,
){
    DeviceItem(
        modifier = modifier,
        deviceName = deviceName,
        rssi = rssi,
        isVisible = isVisible,
        infoText = deviceAdd,
        infoWarnings = false,
        buttonText = stringResource(id = R.string.connect),
        buttonEnable = isVisible,
        onButtonClick = onConnectClick
    )
}

@Composable
fun DeviceItem(
    modifier: Modifier = Modifier,
    deviceName: String,
    rssi: Int,
    isVisible: Boolean,
    infoText: String,
    infoWarnings: Boolean,
    buttonText: String,
    buttonEnable: Boolean,
    onButtonClick: () -> Unit
) {
    Card (
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SignalStrengthIcon(isVisible, rssi)

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))

            // Use a Column to structure the device name and status text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Style the door state text based on visibility and open/close status
                Text(
                    color = if (infoWarnings) Color.Red else LocalContentColor.current,
                    fontWeight = if (infoWarnings) FontWeight.Bold else FontWeight.Normal,
                    text = infoText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Place the button at the end of the row
            Button(
                onClick = onButtonClick,
                enabled = buttonEnable,
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun SignalStrengthIcon(isDeviceVisible: Boolean, rssi: Int) {
    val signalStrengthIcon = when {
        !isDeviceVisible -> R.drawable.rounded_signal_cellular_off_24
        rssi > -40 -> R.drawable.rounded_signal_cellular_4_bar_24
        rssi > -55 -> R.drawable.rounded_signal_cellular_3_bar_24
        rssi > -70 -> R.drawable.rounded_signal_cellular_2_bar_24
        rssi > -80 -> R.drawable.rounded_signal_cellular_1_bar_24
        rssi > -90 -> R.drawable.rounded_signal_cellular_0_bar_24
        else -> R.drawable.rounded_signal_cellular_connected_no_internet_0_bar_24
    }

    // Use a Column to arrange the icon and text vertically
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = signalStrengthIcon),
            contentDescription = stringResource(id = R.string.signal_strength),
            modifier = Modifier.size(dimensionResource(id = R.dimen.rssi_height))
        )
        Spacer(Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
        Text(text = "$rssi dBm")
    }
}

@Preview
@Composable
private fun DeviceItemPreview() {
    DeviceListItem(
        deviceName = "My Device",
        isDoorOpen = false,
        isVisible = false,
        rssi = -54,
        onOpenDoorClick = {}
    )
}

@Preview
@Composable
private fun DeviceScanItemPreview() {
    DeviceScanItem(
        deviceName = "My Device",
        deviceAdd = "46:AF:B8:A6:76:10",
        rssi = -54,
        onConnectClick = {}
    )
}