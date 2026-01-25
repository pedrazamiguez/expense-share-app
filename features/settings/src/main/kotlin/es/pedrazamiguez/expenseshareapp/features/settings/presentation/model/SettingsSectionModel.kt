package es.pedrazamiguez.expenseshareapp.features.settings.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a section in the settings screen.
 */
data class SettingsSectionModel(
    @param:StringRes
    val titleRes: Int, val items: List<SettingsItemModel>
)

/**
 * Represents a single settings item that can be either a standard row or a custom composable.
 */
sealed class SettingsItemModel {
    /**
     * A standard settings row with icon, title, description, and click action.
     */
    data class Standard(
        val icon: ImageVector,
        @param:StringRes
        val titleRes: Int,
        @param:StringRes
        val descriptionRes: Int? = null, val onClick: () -> Unit = {}
    ) : SettingsItemModel()

    /**
     * A settings row with custom trailing content (e.g., a switch).
     */
    data class WithTrailing(
        val icon: ImageVector,
        @param:StringRes
        val titleRes: Int,
        @param:StringRes
        val descriptionRes: Int? = null,
        val onClick: () -> Unit = {},
        val trailingContent: @Composable () -> Unit
    ) : SettingsItemModel()

    /**
     * A settings row with custom description content.
     */
    data class WithCustomDescription(
        val icon: ImageVector,
        @param:StringRes
        val titleRes: Int,
        val onClick: () -> Unit = {},
        val descriptionContent: @Composable () -> Unit
    ) : SettingsItemModel()

    /**
     * A fully custom composable item (e.g., AppVersionFeature, InstallationIdFeature).
     */
    data class Custom(
        val content: @Composable () -> Unit
    ) : SettingsItemModel()
}

