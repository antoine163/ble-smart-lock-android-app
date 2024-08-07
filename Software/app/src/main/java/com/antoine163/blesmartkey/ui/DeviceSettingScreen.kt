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
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antoine163.blesmartkey.R
import com.antoine163.blesmartkey.model.DeviceSetting
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme
import kotlin.math.pow

@Composable
fun DevicesSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceSettingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    DevicesSettingScreen(
        modifier,
        uiState.setting,
        onUnlock = { viewModel.bleDevice.unlock() }
    )
}

@Composable
fun DevicesSettingScreen(
    modifier: Modifier = Modifier,
    deviceSetting: DeviceSetting,
    onUnlock: () -> Unit
) {
    val isConnected: Boolean = deviceSetting.rssi != null

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
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_medium_height)),
                    painter = painterResource(id = R.drawable.rounded_link_off_24),
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
                SignalStrengthIcon(rssi = deviceSetting.rssi)
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
                Text(
                    text = deviceSetting.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isConnected) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_edit_24),
                        contentDescription = stringResource(R.string.edite),
                        Modifier.clickable { /* TODO */ }
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
            enabled = isConnected,
            isUnlock = deviceSetting.isUnlocked,
            isDoorOpen = deviceSetting.isOpened,
            onUnlock = onUnlock,
            onOpenDoor = { /* TODO */ },
            onDisconnect = { /* TODO */ })

        // Night Brightness Card
        NightBrightnessCard(
            modifier = Modifier
                .padding(vertical = dimensionResource(id = R.dimen.padding_tiny))
                .fillMaxWidth(),
            enabled = isConnected,
            currentBrightnessValue = deviceSetting.currentBrightness,
            thresholdValue = deviceSetting.thresholdNight,
            onThresholdChange = { /* TODO */ }
        )

        // Auto Unlock Card
        val currentDistanceValue = when {
            deviceSetting.rssi == null -> null
            else -> calculateDistanceFromRssi(deviceSetting.txPower, deviceSetting.rssi)
        }

        AutoUnlockCard(modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.padding_small))
            .fillMaxWidth(),
            autoUnlock = deviceSetting.autoUnlockEnable,
            unlockDistanceValue = deviceSetting.autoUnlockDistance,
            currentDistanceValue = currentDistanceValue,
            onAutoUnlockChange = { /* TODO */ },
            onUnlockDistanceValue = { /* TODO */ })

    }
}

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

@Composable
fun NightBrightnessCard(
    modifier: Modifier,
    enabled: Boolean,
    currentBrightnessValue: Float,
    thresholdValue: Float,
    onThresholdChange: (Float) -> Unit = {},
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
            enabled = enabled,
            value = "${"%.1f".format(thresholdValue)}%",
            onValueChange = { newValue ->
                onThresholdChange(newValue.toFloatOrNull() ?: 50f)
            },
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.threshold),
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (enabled) {
                        Text(
                            text = stringResource(R.string.current_brightness) + ": ${
                                "%.1f".format(
                                    currentBrightnessValue
                                )
                            }%",
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalContentColor.current.copy(alpha = 0.5f)
                        )
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

@Composable
fun AutoUnlockCard(
    modifier: Modifier,
    autoUnlock: Boolean,
    unlockDistanceValue: Float,
    currentDistanceValue: Float?,
    onAutoUnlockChange: (Boolean) -> Unit,
    onUnlockDistanceValue: (Float) -> Unit
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
            value = "${"%.1f".format(unlockDistanceValue)}m",
            onValueChange = { newValue ->
                onUnlockDistanceValue(newValue.toFloatOrNull() ?: 1.5f)
            },
            enabled = autoUnlock,
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.distance),
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (currentDistanceValue != null && autoUnlock) {
                        Text(
                            text = stringResource(R.string.current_distance) + ": ${
                                "%.1f".format(
                                    currentDistanceValue
                                )
                            }m",
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalContentColor.current.copy(alpha = 0.5f)
                        )
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

fun calculateDistanceFromRssi(txPower: Int, rssi: Int): Float {
    // n is set to 2, but depends on the environment
    val n = 2.0
    return 10.0.pow((txPower - rssi) / (10 * n)).toFloat()
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    showBackground = true,
    device = "id:S21 FE"
)
//@Preview(
//    name = "Light Mode",
//    showBackground = true,
//    device = "id:S21 FE"
//)
@Composable
private fun DevicesSettingScreenPreview() {
    BleSmartKeyTheme {
        Surface {
            DevicesSettingScreen(
                deviceSetting = createDemoDeviceSetting(),
                onUnlock = {}
            )
        }
    }
}

fun createDemoDeviceSetting(): DeviceSetting {
    return DeviceSetting(
        name = "BLE Smart Key",
        address = "12:34:56:78:90:AB",
        rssi = -70,
        isOpened = true,
        isUnlocked = true,
        thresholdNight = 42.8f,
        currentBrightness = 68.7f,
        autoUnlockEnable = true,
        autoUnlockDistance = 1.5f,
        txPower = -14
    )
}


