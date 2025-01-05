package com.antoine163.blesmartkey.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.antoine163.blesmartkey.R
import com.antoine163.blesmartkey.data.DataModule


enum class SmartKeyScreen(
    val id: Int
) {
    Main(id = R.string.app_name),
    Scanning(id = R.string.screen_scanning_name),
    Setting(id = R.string.screen_setting_name),
    Menu(id = R.string.screen_menu_name)
}

@Composable
fun BleSmartKeyScreen(
    navController: NavHostController = rememberNavController(),
    dataModule: DataModule
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = SmartKeyScreen.valueOf(
        backStackEntry?.destination?.route?.substringBefore("/") ?: SmartKeyScreen.Main.name
    )
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            BleSmartKeyAppBar(
                currentScreenName = stringResource(id = currentScreen.id),
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                onMenu = { navController.navigate(SmartKeyScreen.Menu.name) }
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
            // Define a composable function for the SmartKey main screen
            composable(route = SmartKeyScreen.Main.name) {

                // Create and manage the DevicesListViewModel
                val viewModel: DeviceListViewModel = viewModel(
                    factory = DevicesListViewModelFactory(dataModule)
                )

                // Display the DevicesListScreen
                DeviceListScreen(
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                    viewModel = viewModel,
                    onSettingClick = { deviceAdd ->
                        navController.navigate(SmartKeyScreen.Setting.name + "/$deviceAdd")
                    }
                )
            }

            // Define a composable function for the SmartKey menu screen
            composable(route = SmartKeyScreen.Menu.name) {
                MenuScreen(
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))
                )
            }

            // Define a composable function for the SmartKey scanning screen
            composable(route = SmartKeyScreen.Scanning.name) {
                // Create and manage the DevicesScanViewModel
                val viewModel: DeviceScanViewModel = viewModel(
                    factory = DevicesScanViewModelFactory(dataModule)
                )
                val uiState by viewModel.uiState.collectAsState()

                // Display the DevicesScanScreen
                DeviceScanScreen(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                        .fillMaxSize(1f),
                    devices = uiState.devices,
                    onConnectClick = { deviceAdd ->
                        navController.popBackStack(SmartKeyScreen.Main.name, inclusive = false)
                        navController.navigate(SmartKeyScreen.Setting.name + "/$deviceAdd")
                    }
                )
            }

            // Define a composable function for the SmartKey setting screen
            composable(
                route = SmartKeyScreen.Setting.name +"/{deviceAdd}",
                arguments = listOf(navArgument("deviceAdd") { type = NavType.StringType })
            ) { navBackStackEntry ->
                // Retrieve the device address from the navigation arguments
                val deviceAdd = navBackStackEntry.arguments?.getString("deviceAdd") ?: ""

                // Create and manage the DeviceSettingViewModel
                val viewModel: DeviceSettingsViewModel = viewModel(
                    factory = DeviceSettingViewModelFactory(dataModule, deviceAdd)
                )

                // Display the DeviceSettingScreen
                val scrollState = rememberScrollState()
                DeviceSettingsScreen(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                        .fillMaxSize(1f)
                        .verticalScroll(scrollState),
                    viewModel = viewModel,
                    onBack = { navController.navigateUp() },
                    snackbarHostState = snackbarHostState
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
    onMenu: () -> Unit,
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
                IconButton(onClick = onMenu) {
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


//@Preview(
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    name = "Dark Mode",
//    device = "id:S21 FE",
//    showSystemUi = false,
//    showBackground = false
//)
////@Preview(
////    name = "Light Mode",
////    device = "id:S21 FE",
////    showSystemUi = false,
////    showBackground = false
////)
//@Composable
//private fun BleSmartKeyScreenPreview() {
//    BleSmartKeyTheme {
//        BleSmartKeyScreen(
//            devicesBleSettingsRepository = DevicesBleSettingsRepositoryPreview() )
//    }
//}
//
//class DevicesBleSettingsRepositoryPreview() : DevicesBleSettingsRepository {
//
//    private val device1 = DeviceBleSettings.newBuilder().setAddress("12:34:56:78:90:AB").setName("MyDevice").build()
//    private val device2 = DeviceBleSettings.newBuilder().setAddress("AA:BB:CC:DD:EE:FF").setName("AnotherDevice").build()
//    private val devices = DevicesBleSettings.newBuilder().addDevices(device1).addDevices(device2).build()
//
//    private val devicesFlow = MutableStateFlow(devices)
//    override val devicesBleSettingsFlow: Flow<DevicesBleSettings> = devicesFlow
//
//    override suspend fun updateDeviceBleSettings(device: DeviceBleSettings) {}
//}
