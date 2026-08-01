package es.pedrazamiguez.splittrip.features.settlement.presentation.model.archive

import androidx.annotation.StringRes
import es.pedrazamiguez.splittrip.features.settlement.R

enum class ArchiveWizardStep(@StringRes val labelResId: Int) {
    SETTLEMENT_SUMMARY(R.string.archive_wizard_step_summary),
    ACTION_REQUIRED(R.string.archive_wizard_step_action),
    CONFIRMATION(R.string.archive_wizard_step_confirm)
}
