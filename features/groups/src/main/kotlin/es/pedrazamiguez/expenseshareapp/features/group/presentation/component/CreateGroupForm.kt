package es.pedrazamiguez.expenseshareapp.features.group.presentation.component

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.form.FormErrorBanner
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardNavigationBar
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardNavigationBarConfig
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepIndicator
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.GroupCurrencyStep
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.GroupInfoStep
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.GroupMembersStep
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.GroupReviewStep
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupStep
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState

@Composable
fun CreateGroupForm(
    uiState: CreateGroupUiState,
    onEvent: (CreateGroupUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val stepLabels = rememberStepLabels()
    val orderedLabels = remember(uiState.steps, stepLabels) {
        uiState.steps.map { stepLabels[it] ?: "" }
    }

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

            CreateGroupWizardContent(
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
                    backLabel = stringResource(R.string.group_wizard_back),
                    nextLabel = stringResource(R.string.group_wizard_next),
                    submitLabel = stringResource(R.string.groups_create)
                ),
                onBack = { onEvent(CreateGroupUiEvent.PreviousStep) },
                onNext = { onEvent(CreateGroupUiEvent.NextStep) },
                onSubmit = { onEvent(CreateGroupUiEvent.SubmitCreateGroup) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CreateGroupWizardContent(
    uiState: CreateGroupUiState,
    onEvent: (CreateGroupUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = uiState.currentStep,
        modifier = modifier,
        transitionSpec = {
            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
            (slideInHorizontally { it * direction } + fadeIn())
                .togetherWith(slideOutHorizontally { -it * direction } + fadeOut())
                .using(SizeTransform(clip = false))
        },
        label = "createGroupWizardStep"
    ) { step ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (step) {
                CreateGroupStep.INFO -> GroupInfoStep(uiState = uiState, onEvent = onEvent)
                CreateGroupStep.CURRENCY -> GroupCurrencyStep(uiState = uiState, onEvent = onEvent)
                CreateGroupStep.MEMBERS -> GroupMembersStep(uiState = uiState, onEvent = onEvent)
                CreateGroupStep.REVIEW -> {
                    GroupReviewStep(uiState = uiState)
                    FormErrorBanner(
                        error = uiState.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberStepLabels(): Map<CreateGroupStep, String> {
    val infoLabel = stringResource(R.string.group_wizard_step_info)
    val currencyLabel = stringResource(R.string.group_wizard_step_currency)
    val membersLabel = stringResource(R.string.group_wizard_step_members)
    val reviewLabel = stringResource(R.string.group_wizard_step_review)
    return remember(infoLabel, currencyLabel, membersLabel, reviewLabel) {
        mapOf(
            CreateGroupStep.INFO to infoLabel,
            CreateGroupStep.CURRENCY to currencyLabel,
            CreateGroupStep.MEMBERS to membersLabel,
            CreateGroupStep.REVIEW to reviewLabel
        )
    }
}
