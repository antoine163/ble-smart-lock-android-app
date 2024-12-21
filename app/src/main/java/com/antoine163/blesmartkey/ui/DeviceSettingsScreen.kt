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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antoine163.blesmartkey.R
import com.antoine163.blesmartkey.data.model.DeviceSettings
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme


/**
 * Composable function for the device settings screen.
 *
 * This screen displays the current device settings and allows the user to interact with them.
 *
 * @param modifier Modifier to be applied to the layout.
 * @param viewModel The ViewModel that holds the device settings and handles user interactions.
 * @param onBack Callback function to be invoked when the user navigates back from this screen.
 */
@Composable
fun DevicesSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceSettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    DevicesSettingScreen(
        modifier = modifier,
        deviceSettings = uiState.setting,
        onUnlock = { viewModel.bleDevice.unlock() },
        onOpenDoor = {
            viewModel.bleDevice.unlock()
            viewModel.bleDevice.openDoor()
        },
        onDisconnect = {
            viewModel.bleDevice.disconnect()
            onBack()
        },
        onDeviceNameChange = { deviceName -> viewModel.bleDevice.setDeviceName(deviceName) },
        onBrightnessThChange = { brightnessTh -> viewModel.bleDevice.setBrightnessTh(brightnessTh) },
        onAutoUnlockChange = { autoUnlock -> viewModel.autoUnlock(autoUnlock) },
        onUnlockRssiThChange = { unlockRssiTh -> viewModel.setAutoUnlockRssiTh(unlockRssiTh) },
        onDissociate = {
            viewModel.dissociate()
            onBack()
        }
    )
}


/**
 * Screen for device settings.
 *
 * @param modifier Modifier for the root composable.
 * @param deviceSettings The device setting data.
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
    deviceSettings: DeviceSettings,
    onUnlock: () -> Unit,
    onOpenDoor: () -> Unit,
    onDisconnect: () -> Unit,
    onDeviceNameChange: (String) -> Unit,
    onBrightnessThChange: (Float) -> Unit,
    onAutoUnlockChange: (Boolean) -> Unit,
    onUnlockRssiThChange: (Int) -> Unit,
    onDissociate: () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Dissociate Button
            Column(
                modifier = Modifier.clickable { onDissociate() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.icon_small_height)),
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
                    val doorStateIconId = when (deviceSettings.isOpened) {
                        true -> R.drawable.rounded_door_open_24
                        false -> R.drawable.rounded_door_front_24
                    }
                    val doorStateDescId = when (deviceSettings.isOpened) {
                        true -> R.string.state_open
                        false -> R.string.state_close
                    }

                    val lockIconId = when (deviceSettings.isUnlocked) {
                        true -> R.drawable.rounded_lock_open_right_24
                        false -> R.drawable.rounded_lock_24
                    }
                    val lockDescId = when (deviceSettings.isUnlocked) {
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
                SignalStrengthIcon(rssi = deviceSettings.currentRssi)
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
                if (deviceSettings.currentRssi != null) {
                    var showEditDialog by remember { mutableStateOf(false) }
                    if (showEditDialog) {
                        EditValueDialog(
                            initialValue = deviceSettings.name,
                            onConfirm = { newName ->
                                onDeviceNameChange(newName)
                                showEditDialog = false
                            },
                            onDismiss = { showEditDialog = false },
                            title = stringResource(R.string.dlg_dev_name_title),
                            label = stringResource(R.string.dlg_dev_name_label),
                            invalidMessage = stringResource(R.string.dlg_dev_name_invalid).format(16),
                            keyboardType = KeyboardType.Text,
                            valueToString = { it },
                            stringToValue = { if (it.length <= 16) it else null }
                        )
                    }

                    Text(
                        text = deviceSettings.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_edit_24),
                        contentDescription = stringResource(R.string.edite),
                        Modifier.clickable { showEditDialog = true }
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
                text = deviceSettings.address, style = MaterialTheme.typography.bodyMedium
            )
        }


        // Actions Card
        ActionsCard(modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.padding_small))
            .fillMaxWidth(),
            enabled = deviceSettings.currentRssi != null,
            isUnlock = deviceSettings.isUnlocked,
            isDoorOpen = deviceSettings.isOpened,
            onUnlock = onUnlock,
            onOpenDoor = onOpenDoor,
            onDisconnect = onDisconnect)

        // Night Lighting Card
        NightLightingCard(
            modifier = Modifier
                .padding(vertical = dimensionResource(id = R.dimen.padding_tiny))
                .fillMaxWidth(),
            currentBrightness = deviceSettings.currentRssi ?.let { deviceSettings.currentBrightness },
            brightnessTh = deviceSettings.thresholdNight,
            enabled = deviceSettings.currentRssi != null,
            onBrightnessThChange = onBrightnessThChange
        )

        // Auto Unlock Card
        AutoUnlockCard(modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.padding_small))
            .fillMaxWidth(),
            autoUnlock = deviceSettings.autoUnlockEnabled,
            unlockRssiTh = deviceSettings.autoUnlockRssiTh,
            currentRssi = deviceSettings.currentRssi,
            onAutoUnlockChange = onAutoUnlockChange,
            onUnlockRssiThChange = onUnlockRssiThChange)
    }
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
 * A card displaying the night lighting settings.
 *
 * @param modifier Modifier to be applied to the card.
 * @param currentBrightness The current brightness level, or null if unknown.
 * @param brightnessTh The brightness threshold for night lighting.
 * @param onBrightnessThChange Callback to be invoked when the brightness threshold changes.
 */
@Composable
fun NightLightingCard(
    modifier: Modifier,
    currentBrightness: Float?,
    brightnessTh: Float,
    enabled: Boolean,
    onBrightnessThChange: (Float) -> Unit,
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.card_elevation)
        ), modifier = modifier
    ) {
        var showEditDialog by remember { mutableStateOf(false) }
        if (showEditDialog) {
            EditValueDialog(
                initialValue = brightnessTh,
                onConfirm = {
                    onBrightnessThChange(it)
                    showEditDialog = false
                },
                onDismiss = { showEditDialog = false },
                title = stringResource(R.string.dlg_bright_th_title),
                label = stringResource(R.string.dlg_bright_th_label),
                invalidMessage = stringResource(R.string.dlg_bright_th_invalid),
                keyboardType = KeyboardType.Decimal,
                valueToString = { "%.1f".format(it) },
                stringToValue = {
                    val newVal = it.replace(',', '.').toFloatOrNull()
                    if (newVal != null && newVal in 0f..100f) newVal else null
                },
                suffix = "%"
            )
        }

        ParamCard(
            modifier = Modifier,
            title = stringResource(R.string.night_lighting),
            description = stringResource(R.string.night_lighting_info),
            name = stringResource(R.string.bright_th),
            suffix = "%",
            value = "%.1f".format(brightnessTh),
            currentValue = currentBrightness?.let { "%.1f".format(it) },
            onNewValue = { showEditDialog = true },
            enabled = enabled,
            onSetCurrentValue = { currentBrightness ?.let { onBrightnessThChange(it) } },
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
        var showEditDialog by remember { mutableStateOf(false) }
        if (showEditDialog) {
            EditValueDialog(
                initialValue = unlockRssiTh,
                onConfirm = {
                    onUnlockRssiThChange( it )
                    showEditDialog = false
                },
                onDismiss = { showEditDialog = false },
                title = stringResource(R.string.dlg_rssi_title),
                label = stringResource(R.string.dlg_rssi_label),
                invalidMessage = stringResource(R.string.dlg_rssi_invalid).format(-130, 8),
                keyboardType = KeyboardType.Number,
                valueToString = { it.toString() },
                stringToValue = {
                    val newVal = it.toIntOrNull()
                    if (newVal != null && newVal in -130..8) newVal else null
                },
                suffix = "dBm"
            )
        }

        ParamCard(
            modifier = Modifier,
            title = stringResource(R.string.auto_unlock),
            description = stringResource(R.string.auto_unlock_info),
            name = stringResource(R.string.rssi_th),
            suffix = "dBm",
            value = unlockRssiTh.toString(),
            currentValue = currentRssi?.toString(),
            onNewValue = { showEditDialog = true },
            onSetCurrentValue = { currentRssi ?.let { onUnlockRssiThChange(it) } },
            enabled = autoUnlock,
            onEnabledChange = { onAutoUnlockChange(it) }
        )
    }
}

/**
 * A composable card to display and modify a parameter.
 *
 * @param modifier Modifier to be applied to the layout.
 * @param title The title of the parameter.
 * @param description A brief description of the parameter.
 * @param name The name of the parameter.
 * @param suffix The suffix to append to the value (e.g., units).
 * @param value The current value of the parameter.
 * @param currentValue The currently active value (if any).
 * @param onNewValue Callback invoked when the user requests to change the value.
 * @param onSetCurrentValue Callback invoked when the user wants to set the current value.
 * @param enabled Whether the parameter is enabled for modification.
 * @param onEnabledChange Callback invoked when the enabled state changes.
 */
@Composable
fun ParamCard(
    modifier: Modifier,
    title: String,
    description: String,
    name: String,
    suffix: String = "",
    value: String,
    currentValue: String?,
    onNewValue: () -> Unit,
    onSetCurrentValue: () -> Unit,
    enabled: Boolean? = null,
    onEnabledChange: ((Boolean) -> Unit)? = null
) {
    val isEnabled = enabled ?: true

    Column (
        modifier = modifier
    ) {
        // Title
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                    .padding(top = dimensionResource(id = R.dimen.padding_small))
                    .weight(1f)
            )

            // Enabled switch
            onEnabledChange?.let {
                Switch(
                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_small)),
                    checked = isEnabled,
                    onCheckedChange = onEnabledChange
                )
            }
        }

        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.5f),
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.padding_medium),
                end = dimensionResource(id = R.dimen.padding_small)
            )
        )

        // Value
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small))
                .clickable(enabled = isEnabled, onClick = onNewValue)
        ) {
            // Row with name, value, and current value
            Column (
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
            ){
                // Row with name and value
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    // Name
                    Text(
                        modifier = Modifier.weight(1f),
                        text = name,
                        style = MaterialTheme.typography.titleSmall
                    )

                    // Current value
                    if (currentValue != null && isEnabled) {
                        Text(
                            text = stringResource(R.string.current)
                                    + ": ${currentValue}${suffix}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalContentColor.current.copy(alpha = 0.5f)
                        )

                        IconButton(
                            onClick = onSetCurrentValue,
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

                // Value
                Text(
                    text = "${value}${suffix}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * A composable dialog for editing a value of type [T].
 *
 * @param initialValue The initial value to display in the dialog.
 * @param onConfirm A callback invoked when the user confirms the edited value.
 * @param onDismiss A callback invoked when the dialog is dismissed.
 * @param title The title of the dialog.
 * @param label The label for the text field.
 * @param invalidMessage The error message to display when the entered value is invalid.
 * @param keyboardType The keyboard type to use for the text field.
 * @param valueToString A function to convert a value of type [T] to a String.
 * @param stringToValue A function to convert a String to a value of type [T], or null if the string is invalid.
 * @param suffix An optional suffix to display after the text field.
 */
@Composable
fun <T> EditValueDialog(
    initialValue: T,
    onConfirm: (T) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    label: String,
    invalidMessage: String,
    keyboardType: KeyboardType,
    valueToString: (T) -> String,
    stringToValue: (String) -> T?,
    suffix: String = "",
) {
    var value by remember { mutableStateOf( TextFieldValue (valueToString( initialValue ) ) ) }
    var isValid by remember { mutableStateOf( stringToValue( value.text ) != null ) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = { newValue ->
                        value = newValue
                        isValid = stringToValue( newValue.text ) != null
                    },
                    label = { Text(label) },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    trailingIcon = { Text(suffix) },
                    singleLine = true,
                    isError = !isValid
                )

                if (!isValid) {
                    Text(
                        text = invalidMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(
                            start = dimensionResource(R.dimen.padding_medium),
                            top = dimensionResource(R.dimen.padding_small)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm( stringToValue( value.text )!! ) },
                enabled = isValid
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    showBackground = true,
    device = "id:S21 FE"
)
@Composable
private fun EditValueDialogPreview() {
    BleSmartKeyTheme {
        Surface {
            EditValueDialog(
                initialValue = "test",
                onConfirm = {},
                onDismiss = {},
                title = "Title",
                label = "Label",
                invalidMessage = "Invalid message",
                keyboardType = KeyboardType.Text,
                valueToString = { it },
                stringToValue = { it }
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
                deviceSettings = createDemoDeviceSetting(),
                onUnlock = {},
                onOpenDoor = {},
                onDisconnect = {},
                onDeviceNameChange = {},
                onBrightnessThChange = {},
                onAutoUnlockChange = {},
                onUnlockRssiThChange = {},
                onDissociate = {}
            )
        }
    }
}

fun createDemoDeviceSetting(): DeviceSettings {
    return DeviceSettings(
        name = "BLE Smart Lock",
        address = "12:34:56:78:90:AB",
        currentRssi = -70,
        isOpened = true,
        isUnlocked = true,
        thresholdNight = 42.8f,
        currentBrightness = 68.7f,
        autoUnlockEnabled = true,
        autoUnlockRssiTh = -80
    )
}


