package es.pedrazamiguez.splittrip.features.expense.presentation.extensions

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Bed
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Car
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Category
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Coin
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.MasksTheater
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ReceiptRefund
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Run
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Shield
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ShoppingBag
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ToolsKitchen2
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.features.expense.R

@StringRes
fun ExpenseCategory.toStringRes(): Int = when (this) {
    ExpenseCategory.CONTRIBUTION -> R.string.expense_category_contribution
    ExpenseCategory.REFUND -> R.string.expense_category_refund
    ExpenseCategory.TRANSPORT -> R.string.expense_category_transport
    ExpenseCategory.FOOD -> R.string.expense_category_food
    ExpenseCategory.LODGING -> R.string.expense_category_lodging
    ExpenseCategory.ACTIVITIES -> R.string.expense_category_activities
    ExpenseCategory.INSURANCE -> R.string.expense_category_insurance
    ExpenseCategory.ENTERTAINMENT -> R.string.expense_category_entertainment
    ExpenseCategory.SHOPPING -> R.string.expense_category_shopping
    ExpenseCategory.OTHER -> R.string.expense_category_other
}

fun ExpenseCategory.toIconVector(): ImageVector = when (this) {
    ExpenseCategory.CONTRIBUTION -> TablerIcons.Outline.Coin
    ExpenseCategory.REFUND -> TablerIcons.Outline.ReceiptRefund
    ExpenseCategory.TRANSPORT -> TablerIcons.Outline.Car
    ExpenseCategory.FOOD -> TablerIcons.Outline.ToolsKitchen2
    ExpenseCategory.LODGING -> TablerIcons.Outline.Bed
    ExpenseCategory.ACTIVITIES -> TablerIcons.Outline.Run
    ExpenseCategory.INSURANCE -> TablerIcons.Outline.Shield
    ExpenseCategory.ENTERTAINMENT -> TablerIcons.Outline.MasksTheater
    ExpenseCategory.SHOPPING -> TablerIcons.Outline.ShoppingBag
    ExpenseCategory.OTHER -> TablerIcons.Outline.Category
}
