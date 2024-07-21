package com.antoine163.blesmartkey


import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antoine163.blesmartkey.ui.DevicesListScreen
import com.antoine163.blesmartkey.ui.DevicesScanScreen
import com.antoine163.blesmartkey.ui.DevicesSettingScreen
import com.antoine163.blesmartkey.ui.createDemoDeviceList
import com.antoine163.blesmartkey.ui.createDemoDeviceScan
import com.antoine163.blesmartkey.ui.createDemoDeviceSetting
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme


enum class SmartKeyScreen(
    val id: Int
) {
    Main(id = R.string.app_name),
    Scanning(id = R.string.app_name),
    Setting(id = R.string.app_name);
}

@Composable
fun BleSmartKeyApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = SmartKeyScreen.valueOf(
        backStackEntry?.destination?.route ?: SmartKeyScreen.Main.name
    )

    Scaffold(
        topBar = {
            BleSmartKeyAppBar(
                currentScreenName = stringResource(id = currentScreen.id),
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            if (currentScreen == SmartKeyScreen.Main) {
                FloatingActionButton(
                    onClick = { navController.navigate(SmartKeyScreen.Scanning.name) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_add_24),
                        contentDescription = stringResource(R.string.add_device)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SmartKeyScreen.Main.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = SmartKeyScreen.Main.name) {
                DevicesListScreen(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                    onSettingClick = { navController.navigate(SmartKeyScreen.Setting.name) },
                    devices = createDemoDeviceList()
                )
            }

            composable(route = SmartKeyScreen.Scanning.name) {
                DevicesScanScreen(
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_medium))
                        .fillMaxSize(1f),
                    devices = createDemoDeviceScan()
                )
            }

            composable(route = SmartKeyScreen.Setting.name) {
                DevicesSettingScreen(
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_medium))
                        .fillMaxSize(1f),
                    deviceSetting = createDemoDeviceSetting()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleSmartKeyAppBar(
    currentScreenName: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text(
                text = currentScreenName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (!canNavigateBack) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_menu_24),
                        contentDescription = stringResource(R.string.open_menu)
                    )
                }
            } else {
                IconButton(onClick = navigateUp) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },
    )
}


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    device = "id:S21 FE",
    showSystemUi = false,
    showBackground = false
)
//@Preview(
//    name = "Light Mode",
//    device = "id:S21 FE",
//    showSystemUi = false,
//    showBackground = false
//)
@Composable
private fun BleSmartKeyAppPreview() {
    BleSmartKeyTheme {
        BleSmartKeyApp()
    }
}
