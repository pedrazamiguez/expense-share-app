package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.view.SettingItemView

@Composable
fun SettingsRow(item: SettingItemView) {
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
