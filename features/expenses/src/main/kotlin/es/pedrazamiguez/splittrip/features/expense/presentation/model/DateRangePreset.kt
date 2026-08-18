package es.pedrazamiguez.splittrip.features.expense.presentation.model

import androidx.annotation.StringRes
import es.pedrazamiguez.splittrip.features.expense.R

enum class DateRangePreset(@StringRes val titleRes: Int) {
    TODAY(R.string.expenses_filter_date_preset_today),
    YESTERDAY(R.string.expenses_filter_date_preset_yesterday),
    THIS_WEEK(R.string.expenses_filter_date_preset_this_week),
    LAST_15_DAYS(R.string.expenses_filter_date_preset_last_15_days),
    THIS_MONTH(R.string.expenses_filter_date_preset_this_month)
}
