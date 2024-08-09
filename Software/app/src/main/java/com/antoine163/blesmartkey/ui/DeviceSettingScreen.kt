package com.antoine163.blesmartkey.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.antoine163.blesmartkey.R
import com.antoine163.blesmartkey.model.DeviceSetting
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme

/**
 * Composable function representing the Devices Setting Screen.
 *
 * @param modifier Modifier for styling the screen.
 * @param viewModel The ViewModel associated with this screen.
 * @param navController Navigation controller for navigating between screens.
 */
@Composable
fun DevicesSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceSettingViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()

    DevicesSettingScreen(
        modifier = modifier,
        deviceSetting = uiState.setting,
        onUnlock = { viewModel.bleDevice.unlock() },
        onOpenDoor = {
            viewModel.bleDevice.unlock()
            viewModel.bleDevice.openDoor()
        },
        onDisconnect = {
            viewModel.bleDevice.disconnect()
            navController.navigateUp()
        },
        onDeviceNameChange = { deviceName -> viewModel.bleDevice.setDeviceName(deviceName) },
        onBrightnessThChange = { brightnessTh -> viewModel.bleDevice.setBrightnessTh(brightnessTh) },
        onAutoUnlockChange = { autoUnlock -> /* todo */ },
        onUnlockRssiThChange = { unlockRssiTh -> /* todo */ }
    )
}


/**
 * Screen for device settings.
 *
 * @param modifier Modifier for the root composable.
 * @param deviceSetting The device setting data.
 * @param onUnlock Callback when the unlock button is clicked.
 * @param onOpenDoor Callback when the open door button is clicked.
 * @param onDisconnect Callback when the disconnect button is clicked.
 * @param onDeviceNameChange Callback when the device name is changed.
 * @param onBrightnessThChange Callback when the brightness threshold is changed.
 * @param onAutoUnlockChange Callback when the auto unlock setting is changed.
 * @param onUnlockRssiThChange Callback when the unlock RSSI threshold is changed.
 */
@Composable
fun DevicesSettingScreen(
    modifier: Modifier = Modifier,
    deviceSetting: DeviceSetting,
    onUnlock: () -> Unit,
    onOpenDoor: () -> Unit,
    onDisconnect: () -> Unit,
    onDeviceNameChange: (String) -> Unit,
    onBrightnessThChange: (Float) -> Unit,
    onAutoUnlockChange: (Boolean) -> Unit,
    onUnlockRssiThChange: (Int) -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Dissociate Button
            Column(
                modifier = Modifier.clickable { /* TODO */ },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_small_height)),
                    painter = painterResource(R.drawable.rounded_link_off_24),
                    contentDescription = stringResource(R.string.dissociate)
                )
                Text(
                    text = stringResource(R.string.dissociate),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Door Icon
            Column {
                Spacer(modifier = Modifier.padding(top = 50.dp))

                Box {
                    val doorStateIconId = when (deviceSetting.isOpened) {
                        true -> R.drawable.rounded_door_open_24
                        false -> R.drawable.rounded_door_front_24
                    }
                    val doorStateDescId = when (deviceSetting.isOpened) {
                        true -> R.string.state_open
                        false -> R.string.state_close
                    }

                    val lockIconId = when (deviceSetting.isUnlocked) {
                        true -> R.drawable.rounded_lock_open_right_24
                        false -> R.drawable.rounded_lock_24
                    }
                    val lockDescId = when (deviceSetting.isUnlocked) {
                        true -> R.string.unlocked
                        false -> R.string.locked
                    }

                    Icon(
                        painter = painterResource(id = doorStateIconId),
                        contentDescription = stringResource(doorStateDescId),
                        modifier = Modifier.size(180.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(180.dp * 0.2f)
                            .offset(125.dp, 5.dp)
                            .border(3.dp, LocalContentColor.current, CircleShape)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = lockIconId),
                            contentDescription = stringResource(id = lockDescId),
                        )
                    }
                }
            }

            // Signal Strength
            Column {
                SignalStrengthIcon(rssi = deviceSetting.currentRssi)
            }
        }

        // Device Name and Address
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = R.dimen.padding_small))
        ) {
            Row {
                if (deviceSetting.currentRssi != null) {
                    var showDialog by remember { mutableStateOf(false) }
                    if (showDialog) {
                        DialogEditDeviceName(
                            initialDeviceName = deviceSetting.name,
                            onCancel = { showDialog = false },
                            onConfirm = { newName ->
                                onDeviceNameChange(newName)
                                showDialog = false
                            }
                        )
                    }

                    Text(
                        text = deviceSetting.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_edit_24),
                        contentDescription = stringResource(R.string.edite),
                        Modifier.clickable { showDialog = true }
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimensionResource(R.dimen.padding_small)),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
            Text(
                text = deviceSetting.address, style = MaterialTheme.typography.bodyMedium
            )
        }


        // Actions Card
        ActionsCard(modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.padding_small))
            .fillMaxWidth(),
            enabled = deviceSetting.currentRssi != null,
            isUnlock = deviceSetting.isUnlocked,
            isDoorOpen = deviceSetting.isOpened,
            onUnlock = onUnlock,
            onOpenDoor = onOpenDoor,
            onDisconnect = onDisconnect)

        // Night Brightness Card
        NightBrightnessCard(
            modifier = Modifier
                .padding(vertical = dimensionResource(id = R.dimen.padding_tiny))
                .fillMaxWidth(),
            currentBrightness = if (deviceSetting.currentRssi == null) null else deviceSetting.currentBrightness,
            brightnessTh = deviceSetting.thresholdNight,
            onBrightnessThChange = onBrightnessThChange
        )

        // Auto Unlock Card
        AutoUnlockCard(modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.padding_small))
            .fillMaxWidth(),
            autoUnlock = deviceSetting.autoUnlockEnable,
            unlockRssiTh = deviceSetting.autoUnlockRssiTh,
            currentRssi = deviceSetting.currentRssi,
            onAutoUnlockChange = onAutoUnlockChange,
            onUnlockRssiThChange = onUnlockRssiThChange)
    }
}


/**
 * Dialog to edit the name of a device.
 *
 * @param initialDeviceName The initial name of the device.
 * @param onCancel Callback when the user cancels the dialog.
 * @param onConfirm Callback when the user confirms the changes with the new device name.
 */
@Composable
fun DialogEditDeviceName(
    initialDeviceName: String,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var deviceName by remember { mutableStateOf(initialDeviceName) }
    var showSnackbar by remember { mutableStateOf(false) }
    val maxDeviceNameLength = 16

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = stringResource(R.string.edit_lock_name)) },
        text = {
            OutlinedTextField(
                value = deviceName,
                onValueChange = { newText ->
                    if (newText.length <= maxDeviceNameLength) {
                        deviceName = newText
                    } else {
                        showSnackbar = true
                    }
                },
                label = { Text(stringResource(R.string.enter_new_lock_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Snackbar for visual feedback
            if (showSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        stringResource(
                            R.string.device_name_cannot_exceed_characters,
                            maxDeviceNameLength
                        ))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(deviceName) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}


/**
 * A card that displays a set of actions that can be performed.
 *
 * @param modifier Modifier to be applied to the card.
 * @param enabled Whether the actions are enabled.
 * @param isUnlock Whether the unlock action has been performed.
 * @param isDoorOpen Whether the open door action has been performed.
 * @param onUnlock Callback to be invoked when the unlock action is clicked.
 * @param onOpenDoor Callback to be invoked when the open door action is clicked.
 * @param onDisconnect Callback to be invoked when the disconnect action is clicked.
 */
@Composable
fun ActionsCard(
    modifier: Modifier,
    enabled: Boolean,
    isUnlock: Boolean,
    isDoorOpen: Boolean,
    onUnlock: () -> Unit = {},
    onOpenDoor: () -> Unit = {},
    onDisconnect: () -> Unit = {}
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.card_elevation)
        ), modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.actions),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                .padding(top = dimensionResource(id = R.dimen.padding_small))
        )

        Button(
            enabled = !isUnlock && enabled,
            onClick = onUnlock,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.padding_tiny))
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.rounded_lock_open_right_24),
                contentDescription = stringResource(R.string.unlock)
            )
            Spacer(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_tiny))
            )
            Text(text = stringResource(R.string.unlock))
        }

        Button(
            enabled = !isDoorOpen && enabled,
            onClick = onOpenDoor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))

        ) {
            Icon(
                painter = painterResource(id = R.drawable.rounded_door_open_24),
                contentDescription = stringResource(R.string.open_door)
            )
            Spacer(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_tiny))
            )
            Text(text = stringResource(R.string.open_door))
        }

        Button(
            enabled = enabled,
            onClick = onDisconnect,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                .padding(bottom = dimensionResource(id = R.dimen.padding_tiny))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.rounded_logout_24),
                contentDescription = stringResource(R.string.disconnect)
            )
            Spacer(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_tiny))
            )
            Text(text = stringResource(R.string.disconnect))
        }
    }
}


/**
 * A card that displays the current brightness and allows the user to set a brightness threshold.
 *
 * @param modifier Modifier to be applied to the card.
 * @param currentBrightness The current brightness, or null if unknown.
 * @param brightnessTh The brightness threshold.
 * @param onBrightnessThChange Callback to be invoked when the brightness threshold changes.
 */
@Composable
fun NightBrightnessCard(
    modifier: Modifier,
    currentBrightness: Float?,
    brightnessTh: Float,
    onBrightnessThChange: (Float) -> Unit,
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.card_elevation)
        ), modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.night_brightness),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                .padding(top = dimensionResource(id = R.dimen.padding_small))
        )

        Text(
            text = stringResource(R.string.night_brightness_info),
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.5f),
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.padding_medium),
                end = dimensionResource(id = R.dimen.padding_small)
            )
        )

        TextField(
            enabled = currentBrightness != null,
            value = "${"%.1f".format(brightnessTh)}%",
            onValueChange = { newValue ->
                val th = newValue.toFloatOrNull()
                if (th != null) {
                    onBrightnessThChange(th)
                }
            },
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.threshold),
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (currentBrightness != null) {
                        Text(
                            text = stringResource(R.string.current_brightness)
                                    + ": ${"%.1f".format(currentBrightness)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalContentColor.current.copy(alpha = 0.5f)
                        )

                        IconButton(
                            onClick = { onBrightnessThChange(currentBrightness) },
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.icon_tiny_height))
                                .padding(start = dimensionResource(id = R.dimen.padding_tiny))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_arrow_back_24),
                                contentDescription = stringResource(R.string.set_current_value),
                                tint = LocalContentColor.current.copy(alpha = 0.5f),
                                modifier = Modifier.rotate(-90f)
                            )
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
                .fillMaxWidth()
        )
    }
}


/**
 * A Composable function that displays a card for configuring auto-unlock settings.
 *
 * @param modifier Modifier for styling the card.
 * @param autoUnlock Current state of auto-unlock feature.
 * @param unlockRssiTh The RSSI threshold for auto-unlocking.
 * @param currentRssi The current RSSI value.
 * @param onAutoUnlockChange Callback to be invoked when the auto-unlock switch is toggled.
 * @param onUnlockRssiThChange Callback to be invoked when the RSSI threshold is changed.
 */
@Composable
fun AutoUnlockCard(
    modifier: Modifier,
    autoUnlock: Boolean,
    unlockRssiTh: Int,
    currentRssi: Int?,
    onAutoUnlockChange: (Boolean) -> Unit,
    onUnlockRssiThChange: (Int) -> Unit
) {

    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.card_elevation)
        ), modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                .padding(top = dimensionResource(id = R.dimen.padding_small))
        ) {
            Text(
                text = stringResource(R.string.auto_unlock),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = autoUnlock,
                onCheckedChange = onAutoUnlockChange
            )
        }

        Text(
            text = stringResource(R.string.auto_unlock_info),
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.5f),
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.padding_medium),
                end = dimensionResource(id = R.dimen.padding_small)
            )
        )

        TextField(
            value = "${unlockRssiTh}dBm",
            onValueChange = { newValue ->
                val rssiTh = newValue.toIntOrNull()
                if (rssiTh != null) {
                    onUnlockRssiThChange(rssiTh)
                }
            },
            enabled = autoUnlock,
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.rssi_threshold),
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (currentRssi != null && autoUnlock) {
                        Text(
                            text = stringResource(R.string.current_rssi)
                                    + ": ${currentRssi}dBm",
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalContentColor.current.copy(alpha = 0.5f)
                        )

                        IconButton(
                            onClick = { onUnlockRssiThChange(currentRssi) },
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.icon_tiny_height))
                                .padding(start = dimensionResource(id = R.dimen.padding_tiny))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_arrow_back_24),
                                contentDescription = stringResource(R.string.set_current_value),
                                tint = LocalContentColor.current.copy(alpha = 0.5f),
                                modifier = Modifier.rotate(-90f)
                            )
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
                .fillMaxWidth()
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    showBackground = true,
    device = "id:S21 FE"
)
@Composable
private fun DialogEditDeviceNamePreview() {
    BleSmartKeyTheme {
        Surface {
            DialogEditDeviceName(
                initialDeviceName = "BLE Smart Lock",
                onCancel = {},
                onConfirm = {}
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    showBackground = true,
    device = "id:S21 FE"
)
@Composable
private fun DevicesSettingScreenPreview() {
    BleSmartKeyTheme {
        Surface {
            DevicesSettingScreen(
                deviceSetting = createDemoDeviceSetting(),
                onUnlock = {},
                onOpenDoor = {},
                onDisconnect = {},
                onDeviceNameChange = {},
                onBrightnessThChange = {},
                onAutoUnlockChange = {},
                onUnlockRssiThChange = {}
            )
        }
    }
}

fun createDemoDeviceSetting(): DeviceSetting {
    return DeviceSetting(
        name = "BLE Smart Lock",
        address = "12:34:56:78:90:AB",
        currentRssi = -70,
        isOpened = true,
        isUnlocked = true,
        thresholdNight = 42.8f,
        currentBrightness = 68.7f,
        autoUnlockEnable = true,
        autoUnlockRssiTh = -80
    )
}


