package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen

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
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.SharedTransitionSurface
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.component.WizardNavigationBar
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.component.WizardStepIndicator
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.step.AmountStep
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.step.AtmFeeStep
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.step.DetailsStep
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.step.ExchangeRateStep
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.step.ReviewStep
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.CashWithdrawalStep

/**
 * Shared element transition key for the Withdraw Cash FAB -> Screen transition.
 */
const val ADD_CASH_WITHDRAWAL_SHARED_ELEMENT_KEY = "add_cash_withdrawal_container"

@Composable
fun AddCashWithdrawalScreen(
    groupId: String? = null,
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit = {}
) {
    LaunchedEffect(groupId) {
        onEvent(AddCashWithdrawalUiEvent.LoadGroupConfig(groupId))
    }

    SharedTransitionSurface(sharedElementKey = ADD_CASH_WITHDRAWAL_SHARED_ELEMENT_KEY) {
        when {
            uiState.isReady -> {
                WithdrawalWizard(
                    groupId = groupId,
                    uiState = uiState,
                    onEvent = onEvent
                )
            }

            uiState.configLoadFailed -> {
                WithdrawalConfigLoadFailedContent(
                    onRetry = { onEvent(AddCashWithdrawalUiEvent.RetryLoadConfig(groupId)) }
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
private fun WithdrawalWizard(
    groupId: String?,
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val stepLabels = rememberStepLabels()

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WizardStepIndicator(
                steps = uiState.applicableSteps,
                currentStepIndex = uiState.currentStepIndex,
                stepLabels = stepLabels
            )

            WizardStepContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.weight(1f)
            )

            WizardNavigationBar(
                canGoNext = uiState.canGoNext,
                isOnReviewStep = uiState.isOnReviewStep,
                isCurrentStepValid = uiState.isCurrentStepValid,
                isLoading = uiState.isLoading,
                onBack = { onEvent(AddCashWithdrawalUiEvent.PreviousStep) },
                onNext = { onEvent(AddCashWithdrawalUiEvent.NextStep) },
                onSubmit = { onEvent(AddCashWithdrawalUiEvent.SubmitWithdrawal(groupId)) }
            )
        }
    }
}

@Composable
private fun WizardStepContent(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
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
                CashWithdrawalStep.AMOUNT -> AmountStep(uiState = uiState, onEvent = onEvent)
                CashWithdrawalStep.EXCHANGE_RATE -> ExchangeRateStep(uiState = uiState, onEvent = onEvent)
                CashWithdrawalStep.ATM_FEE -> AtmFeeStep(uiState = uiState, onEvent = onEvent)
                CashWithdrawalStep.DETAILS -> DetailsStep(uiState = uiState, onEvent = onEvent)
                CashWithdrawalStep.REVIEW -> ReviewStep(uiState = uiState)
            }
        }
    }
}

@Composable
private fun WithdrawalConfigLoadFailedContent(onRetry: () -> Unit) {
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
            text = stringResource(R.string.withdrawal_error_load_config),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.withdrawal_retry))
        }
    }
}

/**
 * Creates a remembered map of step labels for the wizard indicator.
 */
@Composable
private fun rememberStepLabels(): Map<CashWithdrawalStep, String> {
    val amountLabel = stringResource(R.string.withdrawal_wizard_step_amount)
    val rateLabel = stringResource(R.string.withdrawal_wizard_step_exchange_rate)
    val feeLabel = stringResource(R.string.withdrawal_wizard_step_atm_fee)
    val detailsLabel = stringResource(R.string.withdrawal_wizard_step_details)
    val reviewLabel = stringResource(R.string.withdrawal_wizard_step_review)

    return remember(amountLabel, rateLabel, feeLabel, detailsLabel, reviewLabel) {
        mapOf(
            CashWithdrawalStep.AMOUNT to amountLabel,
            CashWithdrawalStep.EXCHANGE_RATE to rateLabel,
            CashWithdrawalStep.ATM_FEE to feeLabel,
            CashWithdrawalStep.DETAILS to detailsLabel,
            CashWithdrawalStep.REVIEW to reviewLabel
        )
    }
}
