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
    val name: String,
    val description: String,
    val license: String,
    val url: String
)

private val libraries = listOf(
    OssLibrary(
        "Koin",
        "Pragmatic lightweight dependency injection framework for Kotlin",
        "Apache 2.0",
        "https://github.com/InsertKoinIO/koin"
    ),
    OssLibrary(
        "Room",
        "Android Jetpack SQLite object mapping library",
        "Apache 2.0",
        "https://developer.android.com/training/data-storage/room"
    ),
    OssLibrary(
        "Firebase",
        "Google Cloud platform for authentication and syncing",
        "Apache 2.0",
        "https://github.com/firebase/firebase-android-sdk"
    ),
    OssLibrary(
        "Haze",
        "GPU-based real-time blur and glassmorphism effects for Jetpack Compose",
        "Apache 2.0",
        "https://github.com/chrisbanes/haze"
    ),
    OssLibrary(
        "Timber",
        "Extensible utility class for logging with automatic tag generation",
        "Apache 2.0",
        "https://github.com/JakeWharton/timber"
    ),
    OssLibrary(
        "Retrofit",
        "Type-safe REST client for Android and Java by Square",
        "Apache 2.0",
        "https://github.com/square/retrofit"
    ),
    OssLibrary(
        "Coil",
        "Kotlin Coroutine-backed image loading library for Android",
        "Apache 2.0",
        "https://github.com/coil-kt/coil"
    ),
    OssLibrary(
        "Gson",
        "Java library to convert Java Objects into JSON and vice versa",
        "Apache 2.0",
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
                headlineContent = {
                    Text(
                        text = library.name,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                supportingContent = {
                    Column {
                        Text(
                            text = library.description,
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
            )
        }
    }
}
