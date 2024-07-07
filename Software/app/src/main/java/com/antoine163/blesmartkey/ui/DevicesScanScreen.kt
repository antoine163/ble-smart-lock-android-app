package com.antoine163.blesmartkey.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.antoine163.blesmartkey.model.DeviceScanItem
import kotlin.text.substring

/**
 * Composable function to display the device scan screen.
 *
 * @param modifier The modifier to be applied to the root element.
 * @param devices The list of scanned devices.
 */
@Composable
fun DevicesScanScreen(modifier: Modifier = Modifier,
                      devices: List<DeviceScanItem>) {
    if (devices.isEmpty()) {
        EmptyScanResultsCard(modifier)
    } else {
        DeviceList(modifier, devices) {}
    }

    // Voir: https://developer.android.com/quick-guides/content/create-progress-indicator
    // pour ajouter une animation de chargemnt ....
}

/**
 * Composable function for displaying an empty scan results card.
 *
 * @param modifier The modifier to be applied to the card.
 */
@Composable
fun EmptyScanResultsCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.scaning),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            InstructionsText();

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            PairingImage();

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = stringResource(id = R.string.pair_device_information),
                style = MaterialTheme.typography.bodyMedium
            )
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
                color = Color.Black,
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
fun PairingImage() {
    Box {
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
                .size(300.dp*0.2f)
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
    onConnectClick: () -> Unit
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(devices) { index, device ->
            DeviceScanItemScreen(
                modifier = if (index == 0) Modifier else Modifier.padding(
                    top = dimensionResource(id = R.dimen.padding_small)
                ),
                deviceName = device.name,
                deviceAddress = device.address,
                rssi = device.rssi,
                onConnectClick = { onConnectClick() }
            )
        }
    }
}

@Preview
@Composable
private fun DevicesScanScreenPreview() {
    // Create a dummy list of devices for previewing
    val devices = listOf(
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
            rssi = null)
    )

    DevicesScanScreen(devices = devices)
}

@Preview(device = "id:Megane E-Tech")
@Composable
private fun DevicesScanScreenEmptyPreview() {
    val devices: List<DeviceScanItem> = emptyList()
    DevicesScanScreen(devices = devices)
}
