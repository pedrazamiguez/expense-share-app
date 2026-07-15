package es.pedrazamiguez.splittrip.features.settings.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.settings.R

private data class OssLibrary(
    val nameRes: Int,
    val descriptionRes: Int,
    val license: String,
    val url: String
)

private const val LICENSE_APACHE_2_0 = "Apache 2.0"

private val libraries = listOf(
    OssLibrary(
        R.string.open_source_lib_koin_name,
        R.string.open_source_lib_koin_desc,
        LICENSE_APACHE_2_0,
        "https://github.com/InsertKoinIO/koin"
    ),
    OssLibrary(
        R.string.open_source_lib_room_name,
        R.string.open_source_lib_room_desc,
        LICENSE_APACHE_2_0,
        "https://developer.android.com/training/data-storage/room"
    ),
    OssLibrary(
        R.string.open_source_lib_firebase_name,
        R.string.open_source_lib_firebase_desc,
        LICENSE_APACHE_2_0,
        "https://github.com/firebase/firebase-android-sdk"
    ),
    OssLibrary(
        R.string.open_source_lib_haze_name,
        R.string.open_source_lib_haze_desc,
        LICENSE_APACHE_2_0,
        "https://github.com/chrisbanes/haze"
    ),
    OssLibrary(
        R.string.open_source_lib_timber_name,
        R.string.open_source_lib_timber_desc,
        LICENSE_APACHE_2_0,
        "https://github.com/JakeWharton/timber"
    ),
    OssLibrary(
        R.string.open_source_lib_retrofit_name,
        R.string.open_source_lib_retrofit_desc,
        LICENSE_APACHE_2_0,
        "https://github.com/square/retrofit"
    ),
    OssLibrary(
        R.string.open_source_lib_coil_name,
        R.string.open_source_lib_coil_desc,
        LICENSE_APACHE_2_0,
        "https://github.com/coil-kt/coil"
    ),
    OssLibrary(
        R.string.open_source_lib_gson_name,
        R.string.open_source_lib_gson_desc,
        LICENSE_APACHE_2_0,
        "https://github.com/google/gson"
    )
)

@Composable
fun OpenSourceScreen(onLibraryUrlClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = MaterialTheme.spacing.Default)
    ) {
        item {
            Text(
                text = stringResource(R.string.open_source_libraries_header),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(MaterialTheme.spacing.Default)
            )
        }

        items(libraries) { library ->
            ListItem(
                supportingContent = {
                    Column {
                        Text(
                            text = stringResource(library.descriptionRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.open_source_lib_license, library.license),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = MaterialTheme.spacing.Small)
                        )
                    }
                },
                trailingContent = {
                    Text(
                        text = stringResource(R.string.open_source_view_source),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLibraryUrlClick(library.url) }
            ) {
                Text(
                    text = stringResource(library.nameRes),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
