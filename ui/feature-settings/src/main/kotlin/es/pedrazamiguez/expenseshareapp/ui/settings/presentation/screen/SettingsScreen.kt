package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalNavController
import timber.log.Timber

data class SettingItem(
    val icon: ImageVector,
    val title: String,
    val description: String? = null,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        Timber.d("Going back from Settings")
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null
                        )
                    }
                })
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                SettingsSection(title = "General")
            }
            items(
                listOf(
                    SettingItem(
                        icon = Icons.Outlined.AccountCircle,
                        title = "Profile",
                        description = "Edit your profile information",
                        onClick = { navController.navigate("profile") }),
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "Manage notification preferences",
                        onClick = { navController.navigate("notifications") }),
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "Manage notification preferences",
                        onClick = { navController.navigate("notifications") }),
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "Manage notification preferences",
                        onClick = { navController.navigate("notifications") }),
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "Manage notification preferences",
                        onClick = { navController.navigate("notifications") }),
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "Manage notification preferences",
                        onClick = { navController.navigate("notifications") }),
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "Manage notification preferences",
                        onClick = { navController.navigate("notifications") }),
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "Manage notification preferences",
                        onClick = { navController.navigate("notifications") }),
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "Manage notification preferences",
                        onClick = { navController.navigate("notifications") }),
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "Manage notification preferences",
                        onClick = { navController.navigate("notifications") }),
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        description = "Manage notification preferences",
                        onClick = { navController.navigate("notifications") })
                )
            ) { item ->
                SettingsRow(item)
            }

            item {
                SettingsSection(title = "About")
            }
            items(
                listOf(
                    SettingItem(
                        icon = Icons.Outlined.Info,
                        title = "App Info",
                        description = "Version, licenses, etc.",
                        onClick = { navController.navigate("about") })
                )
            ) { item ->
                SettingsRow(item)
            }
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 8.dp
        )
    )
}

@Composable
fun SettingsRow(item: SettingItem) {
    ListItem(
        leadingContent = {
        Icon(
            imageVector = item.icon,
            contentDescription = null
        )
    },
        headlineContent = { Text(item.title) },
        supportingContent = item.description?.let { { Text(it) } },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null
            )
        },
        modifier = Modifier
            .clickable { item.onClick() }
            .fillMaxWidth())
    HorizontalDivider(
        Modifier,
        DividerDefaults.Thickness,
        DividerDefaults.color
    )
}
