package com.antoine163.blesmartkey.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antoine163.blesmartkey.R
import com.antoine163.blesmartkey.data.model.DeviceScanItem
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme


/**
 * Composable function that displays the device scan screen.
 *
 * @param modifier Modifier to be applied to the layout.
 * @param devices List of devices discovered during the scan.
 */
@Composable
fun DeviceScanScreen(
    modifier: Modifier = Modifier,
    devices: List<DeviceScanItem>,
    onConnectClick: (String) -> Unit,
) {
    if (devices.isEmpty()) {
        EmptyScanResults(
            modifier = modifier)
    } else {
        DeviceList(
            modifier = modifier,
            devices = devices,
            onConnectClick = onConnectClick,
        )
    }
}


/**
 * Composable function that displays an empty scan results screen
 * with instructions and an image.
 *
 * @param modifier Modifier to be applied to the layout.
 */
@Composable
fun EmptyScanResults(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(id = R.string.pair_smart_lock),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            InstructionsText()

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            PairingImage(modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            Text(
                text = stringResource(id = R.string.pair_device_information),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.weight(2f))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(id = R.string.scanning),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            CircularProgressIndicator(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.weight(4f))
        }
    }
}




/**
 * Composable function that displays instructions for pairing a device.
 *
 * The function highlights the keyword "BOND" in the instructions text.
 */
@Composable
fun InstructionsText() {
    val instructions = stringResource(id = R.string.pair_device_instructions)
    val keyword = "BOND" // Make the keyword a constant
    val startBondStrIndex = instructions.indexOf(keyword)
    val endBondStrIndex = startBondStrIndex + keyword.length

    val instructionsText = buildAnnotatedString {
        append(instructions.substring(0, startBondStrIndex))
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold
            )
        ) {
            append(keyword)
        }
        append(instructions.substring(endBondStrIndex))
    }

    Text(
        text = instructionsText,
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Composable function that displays an image for the pairing screen.
 *
 * This function creates a Box layout with two Image components.
 * The first Image shows the background image with the pairing device.
 * The second Image shows a smaller image with instructions for the pairing process.
 *
 * @see Image
 * @see Box
 */
@Composable
fun PairingImage(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.bsl_pairing_image),
            contentDescription = stringResource(id = R.string.pair_device_image_description),
            modifier = Modifier.size(300.dp)
        )

        Icon(
            painter = painterResource(id = R.drawable.rounded_touch_app_24),
            contentDescription = stringResource(id = R.string.pair_device_instructions),
            tint = Color.Red,
            modifier = Modifier
                .size(300.dp * 0.2f)
                .offset(145.dp, 75.dp)
                .rotate(-45f)
        )
    }
}

/**
 * Composable function that displays a list of devices that can be connected to.
 *
 * @param modifier The modifier to be applied to the LazyColumn.
 * @param devices A list of [DeviceScanItem] objects representing the devices that can be connected to.
 * @param onConnectClick A callback function that is invoked when the user clicks the "Connect" button.
 *
 */
@Composable
fun DeviceList(
    modifier: Modifier = Modifier,
    devices: List<DeviceScanItem>,
    onConnectClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(devices) { index, device ->
            val topPadding =
                if (index == 0) dimensionResource(id = R.dimen.padding_small) else 0.dp
            DeviceScanItemScreen(
                modifier = Modifier.padding(
                    top = topPadding,
                    bottom = dimensionResource(id = R.dimen.padding_small)
                ),
                deviceName = device.name,
                deviceAddress = device.address,
                rssi = device.rssi,
                onConnectClick = { onConnectClick( device.address ) }
            )
        }
    }
}

//@Preview(
//    name = "Light Mode",
//    showBackground = true,
//    device = "id:S21 FE"
//)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    showBackground = true,
    device = "id:S21 FE"
)
@Composable
private fun DevicesScanScreenPreview() {
    val devices = createDemoDeviceScan()
    BleSmartKeyTheme {
        Surface {
            DeviceScanScreen(devices = devices){}
        }
    }
}

//@Preview(
//    name = "Light Mode",
//    device = "id:S21 FE"
//)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    device = "id:S21 FE"
)
@Composable
private fun DevicesScanScreenEmptyPreview() {
    val devices: List<DeviceScanItem> = emptyList()
    BleSmartKeyTheme {
        Surface {
            DeviceScanScreen(devices = devices){}
        }
    }
}



fun createDemoDeviceScan(): List<DeviceScanItem> {
    // Create a dummy list of devices for previewing
    val devices = listOf<DeviceScanItem>(
        DeviceScanItem(
            name = "Device 1",
            address = "12:34:56:78:90:AB",
            rssi = -55
        ),
        DeviceScanItem(
            name = "Device 2",
            address = "CD:EF:GH:IJ:KL:MN",
            rssi = -60
        ),
        DeviceScanItem(
            name = "Device 3",
            address = "OP:QR:ST:UV:WX:YZ",
            rssi = -70
        ),
        DeviceScanItem(
            name = "Device 4",
            address = "12:34:56:78:90:AB",
            rssi = null),
        DeviceScanItem(
            name = "Device 5",
            address = "12:34:56:78:90:AB",
            rssi = -55
        ),
        DeviceScanItem(
            name = "Device 6",
            address = "CD:EF:GH:IJ:KL:MN",
            rssi = -60
        ),
        DeviceScanItem(
            name = "Device 7",
            address = "OP:QR:ST:UV:WX:YZ",
            rssi = -70
        ),
        DeviceScanItem(
            name = "Device 8",
            address = "12:34:56:78:90:AB",
            rssi = null)
    )

    return devices
}
