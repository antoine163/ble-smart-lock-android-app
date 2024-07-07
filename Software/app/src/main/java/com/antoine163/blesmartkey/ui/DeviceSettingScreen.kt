package com.antoine163.blesmartkey.ui

import android.content.res.Configuration
import android.graphics.LinearGradient
import android.util.Log
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoorBack
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antoine163.blesmartkey.R
import com.antoine163.blesmartkey.model.DeviceSetting
import kotlin.math.pow
import kotlin.text.toFloatOrNull

@Composable
fun DevicesSettingScreen(
    modifier: Modifier = Modifier, device: DeviceSetting
) {

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Dissociate Button
            Column(
                modifier = Modifier.clickable { /*TODO*/ },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_medium_height)),
                    painter = painterResource(id = R.drawable.rounded_link_off_24),
                    contentDescription = stringResource(R.string.dissociate)
                )
                Spacer(Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
                Text(text = stringResource(R.string.dissociate))
            }

            // Door Icon
            Column {
                Spacer(modifier = Modifier.padding(top = 50.dp))
                val doorStateIconId = when {
                    device.isDoorOpen -> R.drawable.rounded_door_open_24
                    else -> R.drawable.rounded_door_front_24
                }
                val doorDescIconId = when {
                    device.isDoorOpen -> R.string.state_open
                    else -> R.string.state_close
                }
                Icon(
                    painter = painterResource(id = doorStateIconId),
                    contentDescription = stringResource(doorDescIconId),
                    modifier = Modifier.size(180.dp)
                )
            }

            // Signal Strength
            Column {
                SignalStrengthIcon(rssi = device.rssi)
            }
        }

        // Device Name and Address
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(painter = painterResource(id = R.drawable.rounded_edit_24),
                    contentDescription = stringResource(R.string.edite),
                    Modifier.clickable { /*TODO*/ })
            }
            Text(
                text = device.address, style = MaterialTheme.typography.bodyMedium
            )
        }


        // Actions Card
        ActionsCard(modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.padding_small))
            .fillMaxWidth(),
            isUnlock = device.isUnlock,
            isDoorOpen = device.isDoorOpen,
            onUnlock = { /*TODO*/ },
            onOpenDoor = { /*TODO*/ },
            onDisconnect = {/*TODO*/ })

        // Night Brightness Card
        NightBrightnessCard(
            modifier = Modifier
                .padding(vertical = dimensionResource(id = R.dimen.padding_tiny))
                .fillMaxWidth(),
            currentBrightnessValue = device.currentBrightness,
            thresholdValue = device.thresholdNight,
            onThresholdChange = { /*TODO*/ },
        )

        // Auto Unlock Card
        val currentDistanceValue = when {
            device.rssi == null -> null
            else -> calculateDistanceFromRssi(device.txPower, device.rssi)
        }

        AutoUnlockCard(modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.padding_small))
            .fillMaxWidth(),
            autoUnlock = device.autoUnlock,
            unlockDistanceValue = device.autoUnlockDistance,
            currentDistanceValue = currentDistanceValue,
            onAutoUnlockChange = { /*TODO*/ },
            onUnlockDistanceValue = { /*TODO*/ })

    }
}

@Composable
fun ActionsCard(
    modifier: Modifier,
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
            enabled = !isUnlock,
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
            enabled = !isDoorOpen,
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
                    Text(
                        text = stringResource(R.string.current_brightness) + ": ${
                            "%.1f".format(
                                currentBrightnessValue
                            )
                        }%",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.5f)
                    )
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
    name = "Light Mode", showBackground = true, device = "id:S21 FE"
)
//@Preview(
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    showBackground = true,
//    name = "Dark Mode"
//)
@Composable
private fun DevicesSettingScreenPreview() {
    val device = DeviceSetting(
        name = "Device 1",
        address = "12:34:56:78:90:AB",
        rssi = -20,
        isDoorOpen = false,
        isUnlock = false,
        thresholdNight = 42.8f,
        currentBrightness = 68.7f,
        autoUnlock = true,
        autoUnlockDistance = 1.5f,
        txPower = -14
    )
    DevicesSettingScreen(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_small)),
        device = device
    )
}






