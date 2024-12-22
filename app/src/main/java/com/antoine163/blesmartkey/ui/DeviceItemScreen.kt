package com.antoine163.blesmartkey.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.antoine163.blesmartkey.R
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme


@Composable
fun DeviceListItemScreen(
    modifier: Modifier = Modifier,
    deviceName: String,
    isDoorOpen: Boolean,
    rssi: Int?,
    isProgressing: Boolean,
    isProgressError: Boolean,
    onOpenDoorClick: () -> Unit,
) {
    DeviceItemScreen(
        modifier = modifier,
        deviceName = deviceName,
        rssi = rssi,
        infoText = if (isDoorOpen) stringResource(id = R.string.state_open) else stringResource(id = R.string.state_close),
        infoWarnings = (rssi == null) && isDoorOpen,
        buttonText = stringResource(id = R.string.open_door),
        isProgressing = isProgressing,
        isProgressError = isProgressError,
        onButtonClick = if ((rssi != null) && !isDoorOpen) onOpenDoorClick else null
    )
}

@Composable
fun DeviceScanItemScreen(
    modifier: Modifier = Modifier,
    deviceName: String,
    deviceAddress: String,
    rssi: Int?,
    isProgressing: Boolean,
    onConnectClick: () -> Unit,
) {
    DeviceItemScreen(
        modifier = modifier,
        deviceName = deviceName,
        rssi = rssi,
        infoText = deviceAddress,
        infoWarnings = false,
        buttonText = stringResource(id = R.string.connect),
        isProgressing = isProgressing,
        isProgressError = false,
        onButtonClick = if (rssi != null) onConnectClick else null
    )
}

@Composable
fun DeviceItemScreen(
    modifier: Modifier = Modifier,
    deviceName: String,
    rssi: Int?,
    infoText: String,
    infoWarnings: Boolean,
    buttonText: String,
    isProgressing: Boolean,
    isProgressError: Boolean,
    onButtonClick: (() -> Unit)?
) {
    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                .padding(vertical = dimensionResource(id = R.dimen.padding_tiny)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SignalStrengthIcon(
                modifier = Modifier.width(55.dp), rssi = rssi
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            // Use a Column to structure the device name and status text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Style the door state text based on visibility and open/close status
                Text(
                    color = if (infoWarnings) Color.Red else LocalContentColor.current,
                    fontWeight = if (infoWarnings) FontWeight.Bold else FontWeight.Normal,
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            // Place the button at the end of the row
            Button(
                onClick = { onButtonClick?.invoke() },
                enabled = onButtonClick != null,
                modifier = Modifier
                    .padding(start = dimensionResource(id = R.dimen.padding_medium))
                    .size(width = 110.dp, height = 40.dp)
            ) {
                Column {

                    Row {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (isProgressing) {
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small_height)),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }

                        if (isProgressError) {
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_error_24),
                                contentDescription = stringResource(id = R.string.error),
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small_height)),
                                tint = Color.Red
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun SignalStrengthIcon(
    modifier: Modifier = Modifier, rssi: Int?
) {
    val signalStrengthIcon = when {
        rssi == null -> R.drawable.rounded_signal_cellular_off_24
        rssi > -40 -> R.drawable.rounded_signal_cellular_4_bar_24
        rssi > -55 -> R.drawable.rounded_signal_cellular_3_bar_24
        rssi > -70 -> R.drawable.rounded_signal_cellular_2_bar_24
        rssi > -80 -> R.drawable.rounded_signal_cellular_1_bar_24
        rssi > -90 -> R.drawable.rounded_signal_cellular_0_bar_24
        else -> R.drawable.rounded_signal_cellular_connected_no_internet_0_bar_24
    }

    // Use a Column to arrange the icon and text vertically
    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = signalStrengthIcon),
            contentDescription = stringResource(id = R.string.signal_strength),
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small_height))
        )
        Text(
            text = if (rssi != null) "$rssi dBm" else stringResource(id = R.string.offline),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

//@Preview(
//    name = "Light Mode",
//    device = "id:S21 FE"
//)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    device = "spec:width=1000px,height=2000px,dpi=480"
)
@Composable
private fun DeviceItemScreenPreview() {
    BleSmartKeyTheme {
        DeviceListItemScreen(
            deviceName = "Ble Smart Lock",
            isDoorOpen = false,
            rssi = -80,
            isProgressing = true,
            isProgressError = true,
            onOpenDoorClick = {})
    }
}

//@Preview(
//    name = "Light Mode",
//    device = "id:S21 FE"
//)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    device = "spec:width=1000px,height=2000px,dpi=480"
)
@Composable
private fun DeviceScanItemScreenPreview() {
    BleSmartKeyTheme {
        DeviceScanItemScreen(
            deviceName = "Ble Smart Lock",
            deviceAddress = "46:AF:B8:A6:76:10",
            rssi = -154,
            isProgressing = false,
            onConnectClick = {})
    }
}