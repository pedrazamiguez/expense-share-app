package es.pedrazamiguez.splittrip.features.group.presentation.model.leave

import androidx.annotation.StringRes
import es.pedrazamiguez.splittrip.features.group.R

enum class LeaveWizardStep(@StringRes val labelResId: Int) {
    BALANCE_SUMMARY(R.string.leave_wizard_step_balance),
    CONFIRMATION(R.string.leave_wizard_step_confirm)
}
