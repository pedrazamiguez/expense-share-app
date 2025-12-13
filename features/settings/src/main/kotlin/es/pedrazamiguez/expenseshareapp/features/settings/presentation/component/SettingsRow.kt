package es.pedrazamiguez.expenseshareapp.features.settings.presentation.component

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
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.view.SettingItemView

@Composable
fun SettingsRow(
    item: SettingItemView,
    descriptionContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {

    ListItem(
        leadingContent = {
            Icon(
                imageVector = item.icon,
                contentDescription = null
            )
        },
        headlineContent = { Text(item.title) },
        supportingContent = {
            if (descriptionContent != null) {
                descriptionContent()
            } else {
                item.description?.let { Text(it) }
            }
        },
        trailingContent = {
            if (trailingContent != null) {
                trailingContent()
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null
                )
            }
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
