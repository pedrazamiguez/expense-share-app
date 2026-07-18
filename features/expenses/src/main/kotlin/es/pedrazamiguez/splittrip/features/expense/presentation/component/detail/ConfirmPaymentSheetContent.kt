package es.pedrazamiguez.splittrip.features.expense.presentation.component.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.FormSubmitButton
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.SheetTitleText
import es.pedrazamiguez.splittrip.features.expense.R
import es.pedrazamiguez.splittrip.features.expense.presentation.model.ExpenseDetailUiModel

@Composable
internal fun ConfirmPaymentSheetContent(
    expense: ExpenseDetailUiModel,
    amountText: String,
    isError: Boolean,
    onAmountChange: (String) -> Unit,
    onConfirmClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.Screen)
            .padding(bottom = MaterialTheme.spacing.ExtraLarge)
    ) {
        SheetTitleText(
            text = stringResource(R.string.confirm_payment_amount_title),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.Medium))

        CaptionText(
            text = stringResource(
                R.string.confirm_payment_original_amount_label,
                expense.formattedSourceAmount.orEmpty()
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.ExtraSmall))
        CaptionText(
            text = stringResource(
                R.string.confirm_payment_expected_charge_label,
                expense.formattedExpectedGroupAmount.orEmpty()
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.Large))

        StyledOutlinedTextField(
            value = amountText,
            onValueChange = onAmountChange,
            label = stringResource(R.string.confirm_payment_actual_amount_field_label, expense.groupCurrency),
            placeholder = expense.formattedConfirmPaymentPlaceholder,
            isError = isError,
            supportingText = if (isError) stringResource(R.string.expense_error_confirm_payment_failed) else null,
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { onConfirmClicked() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.Large))

        FormSubmitButton(
            label = stringResource(R.string.confirm_payment),
            isEnabled = true,
            isLoading = false,
            onSubmit = onConfirmClicked,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
