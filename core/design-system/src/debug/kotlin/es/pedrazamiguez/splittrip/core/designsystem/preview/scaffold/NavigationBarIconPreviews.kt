package es.pedrazamiguez.splittrip.core.designsystem.preview.scaffold

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Home
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Receipt
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.User
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.UsersGroup
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.NavigationBarIcon
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemes

@PreviewThemes
@Composable
private fun NavigationBarIconSelectedPreview() {
    PreviewThemeWrapper {
        Row(modifier = Modifier.padding(16.dp)) {
            NavigationBarIcon(
                icon = TablerIcons.Outline.Home,
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
                icon = TablerIcons.Outline.Home,
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
                icon = TablerIcons.Outline.UsersGroup,
                contentDescription = "Groups",
                isSelected = true
            )
            Spacer(Modifier.width(24.dp))
            NavigationBarIcon(
                icon = TablerIcons.Outline.Receipt,
                contentDescription = "Expenses",
                isSelected = false
            )
            Spacer(Modifier.width(24.dp))
            NavigationBarIcon(
                icon = TablerIcons.Outline.User,
                contentDescription = "Profile",
                isSelected = false
            )
        }
    }
}
