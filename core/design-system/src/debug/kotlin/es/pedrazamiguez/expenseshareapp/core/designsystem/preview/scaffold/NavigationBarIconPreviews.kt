package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.scaffold

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemes

@PreviewThemes
@Composable
private fun NavigationBarIconSelectedPreview() {
    PreviewThemeWrapper {
        Row(modifier = Modifier.padding(16.dp)) {
            NavigationBarIcon(
                icon = Icons.Filled.Home,
                contentDescription = "Home",
                isSelected = true
            )
        }
    }
}

@PreviewThemes
@Composable
private fun NavigationBarIconUnselectedPreview() {
    PreviewThemeWrapper {
        Row(modifier = Modifier.padding(16.dp)) {
            NavigationBarIcon(
                icon = Icons.Outlined.Home,
                contentDescription = "Home",
                isSelected = false
            )
        }
    }
}

@PreviewThemes
@Composable
private fun NavigationBarIconsRowPreview() {
    PreviewThemeWrapper {
        Row(modifier = Modifier.padding(16.dp)) {
            NavigationBarIcon(
                icon = Icons.Filled.Groups,
                contentDescription = "Groups",
                isSelected = true
            )
            Spacer(Modifier.width(24.dp))
            NavigationBarIcon(
                icon = Icons.Outlined.Receipt,
                contentDescription = "Expenses",
                isSelected = false
            )
            Spacer(Modifier.width(24.dp))
            NavigationBarIcon(
                icon = Icons.Outlined.Person,
                contentDescription = "Profile",
                isSelected = false
            )
        }
    }
}
