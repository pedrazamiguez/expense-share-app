package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardNavigationBar
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardNavigationBarConfig
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepIndicator
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.SharedTransitionSurface
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.step.contribution.ContributionAmountStep
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.step.contribution.ContributionReviewStep
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.step.contribution.ContributionScopeStep
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddContributionUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddContributionStep
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddContributionUiState

/**
 * Shared element transition key for the Add Money FAB -> Screen transition.
 */
const val ADD_CONTRIBUTION_SHARED_ELEMENT_KEY = "add_contribution_container"

@Composable
fun AddContributionScreen(
    groupId: String? = null,
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit = {}
) {
    LaunchedEffect(groupId) {
        onEvent(AddContributionUiEvent.LoadSubunitOptions(groupId))
    }

    SharedTransitionSurface(sharedElementKey = ADD_CONTRIBUTION_SHARED_ELEMENT_KEY) {
        ContributionWizard(
            groupId = groupId,
            uiState = uiState,
            onEvent = onEvent
        )
    }
}

@Composable
private fun ContributionWizard(
    groupId: String?,
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val stepLabelMap = rememberStepLabelMap()
    val orderedLabels = remember(uiState.steps, stepLabelMap) {
        uiState.steps.map { stepLabelMap[it] ?: "" }
    }

    val backLabel = stringResource(R.string.contribution_wizard_back)
    val nextLabel = stringResource(R.string.contribution_wizard_next)
    val submitLabel = stringResource(R.string.balances_add_money_submit)

    Box(
        modifier = modifier
            .fillMaxSize()
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
                onBack = { onEvent(AddContributionUiEvent.PreviousStep) },
                onNext = { onEvent(AddContributionUiEvent.NextStep) },
                onSubmit = { onEvent(AddContributionUiEvent.Submit(groupId)) }
            )
        }
    }
}

@Composable
private fun WizardStepContent(
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit,
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
        label = "contributionWizardStep"
    ) { step ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            when (step) {
                AddContributionStep.AMOUNT -> ContributionAmountStep(
                    uiState = uiState,
                    onEvent = onEvent,
                    onSubmitKeyboard = { onEvent(AddContributionUiEvent.NextStep) }
                )
                AddContributionStep.SCOPE -> ContributionScopeStep(
                    uiState = uiState,
                    onEvent = onEvent
                )
                AddContributionStep.REVIEW -> ContributionReviewStep(
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
private fun rememberStepLabelMap(): Map<AddContributionStep, String> {
    val amountLabel = stringResource(R.string.contribution_wizard_step_amount)
    val scopeLabel = stringResource(R.string.contribution_wizard_step_scope)
    val reviewLabel = stringResource(R.string.contribution_wizard_step_review)

    return remember(amountLabel, scopeLabel, reviewLabel) {
        mapOf(
            AddContributionStep.AMOUNT to amountLabel,
            AddContributionStep.SCOPE to scopeLabel,
            AddContributionStep.REVIEW to reviewLabel
        )
    }
}
