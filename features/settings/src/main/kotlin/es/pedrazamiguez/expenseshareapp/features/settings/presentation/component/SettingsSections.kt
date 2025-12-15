package es.pedrazamiguez.expenseshareapp.features.settings.presentation.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.model.SettingsItemModel
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.model.SettingsSectionModel
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.view.SettingItemView

/**
 * Extension function to add settings sections to a LazyColumn.
 * This makes the settings screen more maintainable by separating
 * the data structure from the UI rendering logic.
 */
fun LazyListScope.settingsSections(sections: List<SettingsSectionModel>) {
    sections.forEach { section ->
        item(key = "section_${section.titleRes}") {
            SettingsSectionHeader(titleRes = section.titleRes)
        }

        items(
            items = section.items,
            key = { item ->
                when (item) {
                    is SettingsItemModel.Standard -> "item_${item.titleRes}"
                    is SettingsItemModel.WithTrailing -> "item_trailing_${item.titleRes}"
                    is SettingsItemModel.WithCustomDescription -> "item_custom_desc_${item.titleRes}"
                    is SettingsItemModel.Custom -> "item_custom_${item.hashCode()}"
                }
            }
        ) { item ->
            SettingsItemContent(item = item)
        }
    }
}

@Composable
private fun SettingsSectionHeader(titleRes: Int) {
    SettingsSection(title = stringResource(titleRes))
}

@Composable
private fun SettingsItemContent(item: SettingsItemModel) {
    when (item) {
        is SettingsItemModel.Standard -> {
            SettingsRow(
                item = SettingItemView(
                    icon = item.icon,
                    title = stringResource(item.titleRes),
                    description = item.descriptionRes?.let { stringResource(it) },
                    onClick = item.onClick
                )
            )
        }

        is SettingsItemModel.WithTrailing -> {
            SettingsRow(
                item = SettingItemView(
                    icon = item.icon,
                    title = stringResource(item.titleRes),
                    description = item.descriptionRes?.let { stringResource(it) },
                    onClick = item.onClick
                ),
                trailingContent = item.trailingContent
            )
        }

        is SettingsItemModel.WithCustomDescription -> {
            SettingsRow(
                item = SettingItemView(
                    icon = item.icon,
                    title = stringResource(item.titleRes),
                    description = null,
                    onClick = item.onClick
                ),
                descriptionContent = item.descriptionContent
            )
        }

        is SettingsItemModel.Custom -> {
            item.content()
        }
    }
}

