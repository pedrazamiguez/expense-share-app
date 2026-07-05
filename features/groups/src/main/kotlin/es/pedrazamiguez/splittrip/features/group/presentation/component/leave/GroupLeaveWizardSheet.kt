package es.pedrazamiguez.splittrip.features.group.presentation.component.leave

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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

@Suppress("LongMethod")
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
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        val activeSteps = leaveWizardState.activeSteps
        val currentStepIndex = activeSteps.indexOf(leaveWizardState.currentStep).coerceAtLeast(0)
        val isOnLastStep = leaveWizardState.currentStep == activeSteps.lastOrNull()

        Column(modifier = Modifier.fillMaxWidth()) {
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
                            modifier = Modifier.padding(bottom = 80.dp)
                        )
                    }
                    LeaveWizardStep.SETTLEMENTS -> {
                        LeaveSettlementStep(
                            settlements = leaveWizardState.settlements,
                            onConfirmSettlement = onConfirmSettlement,
                            modifier = Modifier.padding(bottom = 80.dp)
                        )
                    }
                    LeaveWizardStep.CASH_RESOLUTION -> {
                        LeaveCashResolutionStep(
                            cashResolution = leaveWizardState.cashResolution,
                            modifier = Modifier.padding(bottom = 80.dp)
                        )
                    }
                    LeaveWizardStep.CONFIRMATION -> {
                        LeaveConfirmationStep(
                            groupName = groupName,
                            subunitImpact = leaveWizardState.subunitImpact,
                            modifier = Modifier.padding(bottom = 80.dp)
                        )
                    }
                }
            }

            WizardNavigationBar(
                config = WizardNavigationBarConfig(
                    canGoNext = true,
                    isOnLastStep = isOnLastStep,
                    isCurrentStepValid = true,
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
