package es.pedrazamiguez.splittrip.features.expense.presentation.component.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.FormSubmitButton
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.SheetTitleText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.parseAmountToSmallestUnit
import es.pedrazamiguez.splittrip.features.expense.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfirmPaymentBottomSheet(
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
    formattedSourceAmount: String,
    formattedExpectedGroupAmount: String,
    groupCurrency: String
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val validateAndConfirm: () -> Unit = {
        val parsed = runCatching {
            parseAmountToSmallestUnit(amountText, groupCurrency)
        }.getOrNull()
        if (parsed != null && parsed > 0) {
            onConfirm(parsed)
        } else {
            isError = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        ConfirmPaymentSheetContent(
            formattedSourceAmount = formattedSourceAmount,
            formattedExpectedGroupAmount = formattedExpectedGroupAmount,
            groupCurrency = groupCurrency,
            amountText = amountText,
            isError = isError,
            onAmountChange = {
                amountText = it
                isError = false
            },
            onConfirmClicked = validateAndConfirm
        )
    }
}

@Composable
private fun ConfirmPaymentSheetContent(
    formattedSourceAmount: String,
    formattedExpectedGroupAmount: String,
    groupCurrency: String,
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
            text = stringResource(R.string.confirm_payment_original_amount_label, formattedSourceAmount),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.ExtraSmall))
        CaptionText(
            text = stringResource(R.string.confirm_payment_expected_charge_label, formattedExpectedGroupAmount),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.Large))

        StyledOutlinedTextField(
            value = amountText,
            onValueChange = onAmountChange,
            label = stringResource(R.string.confirm_payment_actual_amount_field_label, groupCurrency),
            placeholder = "0.00",
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
