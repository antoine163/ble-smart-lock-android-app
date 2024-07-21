package com.antoine163.blesmartkey


import android.content.res.Configuration
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
import com.antoine163.blesmartkey.ui.DevicesListScreen
import com.antoine163.blesmartkey.ui.DevicesScanScreen
import com.antoine163.blesmartkey.ui.createDemoDeviceList
import com.antoine163.blesmartkey.ui.createDemoDeviceScan
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleSmartKeyApp() {
    //val scrollBehavior = TopAppBarScrollBehavior()

    var addingDevice by remember { mutableStateOf( false ) }

    Scaffold(
        //modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_menu_24),
                            contentDescription = stringResource(R.string.open_menu)
                        )
                    }
                },
                //scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if ( !addingDevice ) {
                FloatingActionButton(
                    onClick = { addingDevice = true },
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
        if ( !addingDevice ) {
            DevicesListScreen(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                contentPadding = innerPadding,
                devices = createDemoDeviceList()
            )
        }
        else {
            DevicesScanScreen(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                contentPadding = innerPadding,
                devices = createDemoDeviceScan()
            )
        }
    }
}


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    device = "id:S21 FE",
    showSystemUi = false,
    showBackground = false
)
@Preview(
    name = "Light Mode",
    device = "id:S21 FE",
    showSystemUi = false,
    showBackground = false
)
@Composable
private fun BleSmartKeyAppPreview () {
    BleSmartKeyTheme {
        BleSmartKeyApp()
    }
}
