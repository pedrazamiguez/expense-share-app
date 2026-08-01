package es.pedrazamiguez.splittrip.features.group.presentation.component.leave

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardNavigationBar
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardNavigationBarConfig
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStepIndicator
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.component.step.leave.LeaveBalanceSummaryStep
import es.pedrazamiguez.splittrip.features.group.presentation.component.step.leave.LeaveCashResolutionStep
import es.pedrazamiguez.splittrip.features.group.presentation.component.step.leave.LeaveConfirmationStep
import es.pedrazamiguez.splittrip.features.group.presentation.component.step.leave.LeaveSettlementStep
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardStep
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardUiState

private val StepBottomPadding = 80.dp

@Suppress("LongMethod", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupLeaveWizardSheet(
    groupName: String,
    leaveWizardState: LeaveWizardUiState,
    onNextClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmSettlement: (String) -> Unit,
    onConfirmLeave: () -> Unit,
    onGoToSettlementsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState =
        rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
        contentWindowInsets = { WindowInsets.safeDrawing },
        modifier = modifier
    ) {
        val activeSteps = leaveWizardState.activeSteps
        val currentStepIndex = activeSteps.indexOf(leaveWizardState.currentStep).coerceAtLeast(0)
        val isOnLastStep = leaveWizardState.currentStep == activeSteps.lastOrNull()

        val isCurrentStepValid = when (leaveWizardState.currentStep) {
            LeaveWizardStep.SETTLEMENTS -> leaveWizardState.settlements.all { it.isConfirmed }
            LeaveWizardStep.CONFIRMATION -> leaveWizardState.settlements.all { it.isConfirmed }
            else -> true
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (activeSteps.isNotEmpty()) {
                WizardStepIndicator(
                    stepLabels = activeSteps.map { stringResource(it.labelResId) },
                    currentStepIndex = currentStepIndex
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                when (leaveWizardState.currentStep) {
                    LeaveWizardStep.BALANCE_SUMMARY -> {
                        LeaveBalanceSummaryStep(
                            balanceSummary = leaveWizardState.balanceSummary,
                            modifier = Modifier.padding(bottom = StepBottomPadding)
                        )
                    }
                    LeaveWizardStep.SETTLEMENTS -> {
                        LeaveSettlementStep(
                            settlements = leaveWizardState.settlements,
                            onConfirmSettlement = onConfirmSettlement,
                            modifier = Modifier.padding(bottom = StepBottomPadding)
                        )
                    }
                    LeaveWizardStep.CASH_RESOLUTION -> {
                        LeaveCashResolutionStep(
                            cashResolution = leaveWizardState.cashResolution,
                            modifier = Modifier.padding(bottom = StepBottomPadding)
                        )
                    }
                    LeaveWizardStep.CONFIRMATION -> {
                        val hasUnresolved = leaveWizardState.settlements.any { !it.isConfirmed }
                        LeaveConfirmationStep(
                            groupName = groupName,
                            subunitImpact = leaveWizardState.subunitImpact,
                            hasUnresolvedSettlements = hasUnresolved,
                            onGoToSettlementsClicked = onGoToSettlementsClicked,
                            modifier = Modifier.padding(bottom = StepBottomPadding)
                        )
                    }
                }
            }

            WizardNavigationBar(
                config = WizardNavigationBarConfig(
                    canGoNext = isCurrentStepValid,
                    isOnLastStep = isOnLastStep,
                    isCurrentStepValid = isCurrentStepValid,
                    isLoading = leaveWizardState.isLoading,
                    backLabel = stringResource(R.string.group_wizard_back),
                    nextLabel = stringResource(R.string.group_wizard_next),
                    submitLabel = stringResource(R.string.leave_wizard_submit_button)
                ),
                onBack = onBackClicked,
                onNext = onNextClicked,
                onSubmit = onConfirmLeave
            )
        }
    }
}
