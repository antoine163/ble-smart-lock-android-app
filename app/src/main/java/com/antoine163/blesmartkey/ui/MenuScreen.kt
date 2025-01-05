package com.antoine163.blesmartkey.ui

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antoine163.blesmartkey.BuildConfig
import com.antoine163.blesmartkey.R
import com.antoine163.blesmartkey.ui.theme.BleSmartKeyTheme

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
) {
    val buildDateMillis = BuildConfig.BUILD_TIMESTAMP.toLong()
    val buildDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date(buildDateMillis))

    AppInfoScreen(
        modifier = modifier,
        appName = stringResource(R.string.app_name),
        appVersion = BuildConfig.VERSION_NAME,
        websiteUrl = "https://github.com/antoine163/ble-smart-lock/blob/master/README.md",
        imageRes = R.drawable.ic_launcher_foreground,
        author = "Antoine163",
        buildDate = buildDate,
    )
}

@Composable
fun AppInfoScreen(
    appName: String,
    appVersion: String,
    websiteUrl: String,
    imageRes: Int,
    author: String,
    buildDate: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // App Name
        Text(
            text = appName,
            style = MaterialTheme.typography.titleLarge
        )

        // App Icon
        Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)))
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = stringResource(R.string.app_icon),
            modifier = Modifier
                .size(100.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)))

        // App Version
        Text(
            text = stringResource(R.string.version, appVersion),
            style = MaterialTheme.typography.bodyMedium
        )

        // Website Link
        TextButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                val uri = Uri.parse(websiteUrl)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }) {
            Text(
                text = stringResource(R.string.see_on_github),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Author
        Text(
            text = stringResource(id = R.string.author, author),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Build Date
        Text(
            text = stringResource(id = R.string.build_date, buildDate),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )


    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    device = "id:S21 FE"
)
@Composable
private fun MenuScreenPreview() {
    BleSmartKeyTheme {
        Surface {
            MenuScreen()
        }
    }
}