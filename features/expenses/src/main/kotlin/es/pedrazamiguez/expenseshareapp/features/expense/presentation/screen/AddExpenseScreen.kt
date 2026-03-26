package es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardNavigationBar
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardNavigationBarConfig
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepIndicator
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.SharedTransitionSurface
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.AddOnsStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.AmountStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.CategoryStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.ExchangeRateStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.PaymentMethodStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.PaymentStatusStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.ReceiptStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.ReviewStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.SplitStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.TitleStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense.VendorNotesStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseStep
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Shared element transition key for the Add Expense FAB -> Screen transition.
 */
const val ADD_EXPENSE_SHARED_ELEMENT_KEY = "add_expense_container"

@Composable
fun AddExpenseScreen(
    groupId: String? = null,
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit = {}
) {
    LaunchedEffect(groupId) {
        onEvent(AddExpenseUiEvent.LoadGroupConfig(groupId))
    }

    SharedTransitionSurface(sharedElementKey = ADD_EXPENSE_SHARED_ELEMENT_KEY) {
        when {
            uiState.isReady -> {
                ExpenseWizard(groupId = groupId, uiState = uiState, onEvent = onEvent)
            }

            uiState.configLoadFailed -> {
                AddExpenseConfigFailedContent(
                    onRetry = { onEvent(AddExpenseUiEvent.RetryLoadConfig(groupId)) }
                )
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ExpenseWizard(
    groupId: String?,
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val stepLabelMap = rememberStepLabelMap()
    val orderedLabels = remember(uiState.applicableSteps, stepLabelMap) {
        uiState.applicableSteps.map { stepLabelMap[it] ?: "" }
    }

    val backLabel = stringResource(R.string.expense_wizard_back)
    val nextLabel = stringResource(R.string.expense_wizard_next)
    val submitLabel = stringResource(R.string.add_expense_submit_button)

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WizardStepIndicator(
                stepLabels = orderedLabels,
                currentStepIndex = uiState.currentStepIndex
            )

            WizardStepContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.weight(1f)
            )

            WizardNavigationBar(
                config = WizardNavigationBarConfig(
                    canGoNext = uiState.canGoNext,
                    isOnLastStep = uiState.isOnReviewStep,
                    isCurrentStepValid = uiState.isCurrentStepValid,
                    isLoading = uiState.isLoading,
                    backLabel = backLabel,
                    nextLabel = nextLabel,
                    submitLabel = submitLabel
                ),
                onBack = { onEvent(AddExpenseUiEvent.PreviousStep) },
                onNext = { onEvent(AddExpenseUiEvent.NextStep) },
                onSubmit = { onEvent(AddExpenseUiEvent.SubmitAddExpense(groupId)) }
            )
        }
    }
}

@Composable
private fun WizardStepContent(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = uiState.currentStep,
        modifier = modifier,
        transitionSpec = {
            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
            (slideInHorizontally { fullWidth -> direction * fullWidth } + fadeIn())
                .togetherWith(
                    slideOutHorizontally { fullWidth -> -direction * fullWidth } + fadeOut()
                )
                .using(SizeTransform(clip = false))
        },
        label = "wizardStep"
    ) { step ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (step) {
                AddExpenseStep.TITLE -> TitleStep(uiState = uiState, onEvent = onEvent)
                AddExpenseStep.PAYMENT_METHOD -> PaymentMethodStep(uiState = uiState, onEvent = onEvent)
                AddExpenseStep.AMOUNT -> AmountStep(uiState = uiState, onEvent = onEvent)
                AddExpenseStep.EXCHANGE_RATE -> ExchangeRateStep(uiState = uiState, onEvent = onEvent)
                AddExpenseStep.SPLIT -> SplitStep(uiState = uiState, onEvent = onEvent)
                AddExpenseStep.CATEGORY -> CategoryStep(uiState = uiState, onEvent = onEvent)
                AddExpenseStep.VENDOR_NOTES -> VendorNotesStep(uiState = uiState, onEvent = onEvent)
                AddExpenseStep.PAYMENT_STATUS -> PaymentStatusStep(uiState = uiState, onEvent = onEvent)
                AddExpenseStep.RECEIPT -> ReceiptStep(uiState = uiState, onEvent = onEvent)
                AddExpenseStep.ADD_ONS -> AddOnsStep(uiState = uiState, onEvent = onEvent)
                AddExpenseStep.REVIEW -> ReviewStep(uiState = uiState)
            }
        }
    }
}

@Composable
private fun AddExpenseConfigFailedContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.expense_error_load_group_config),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.expense_error_config_retry_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.expense_error_retry_button))
        }
    }
}

/**
 * Creates a remembered map of [AddExpenseStep] → localised label.
 *
 * Using a map (not a list) keeps the table readable and ensures that
 * [ExpenseWizard] can derive the correctly-ordered list from the dynamic
 * [AddExpenseUiState.applicableSteps] at composition time.
 */
@Composable
private fun rememberStepLabelMap(): Map<AddExpenseStep, String> {
    val titleLabel = stringResource(R.string.expense_wizard_step_title)
    val paymentMethodLabel = stringResource(R.string.expense_wizard_step_payment_method)
    val amountLabel = stringResource(R.string.expense_wizard_step_amount)
    val rateLabel = stringResource(R.string.expense_wizard_step_exchange_rate)
    val splitLabel = stringResource(R.string.expense_wizard_step_split)
    val categoryLabel = stringResource(R.string.expense_wizard_step_category)
    val vendorNotesLabel = stringResource(R.string.expense_wizard_step_vendor_notes)
    val paymentStatusLabel = stringResource(R.string.expense_wizard_step_payment_status)
    val receiptLabel = stringResource(R.string.expense_wizard_step_receipt)
    val addOnsLabel = stringResource(R.string.expense_wizard_step_add_ons)
    val reviewLabel = stringResource(R.string.expense_wizard_step_review)

    return remember(
        titleLabel, paymentMethodLabel, amountLabel, rateLabel, splitLabel,
        categoryLabel, vendorNotesLabel, paymentStatusLabel, receiptLabel, addOnsLabel, reviewLabel
    ) {
        mapOf(
            AddExpenseStep.TITLE to titleLabel,
            AddExpenseStep.PAYMENT_METHOD to paymentMethodLabel,
            AddExpenseStep.AMOUNT to amountLabel,
            AddExpenseStep.EXCHANGE_RATE to rateLabel,
            AddExpenseStep.SPLIT to splitLabel,
            AddExpenseStep.CATEGORY to categoryLabel,
            AddExpenseStep.VENDOR_NOTES to vendorNotesLabel,
            AddExpenseStep.PAYMENT_STATUS to paymentStatusLabel,
            AddExpenseStep.RECEIPT to receiptLabel,
            AddExpenseStep.ADD_ONS to addOnsLabel,
            AddExpenseStep.REVIEW to reviewLabel
        )
    }
}
