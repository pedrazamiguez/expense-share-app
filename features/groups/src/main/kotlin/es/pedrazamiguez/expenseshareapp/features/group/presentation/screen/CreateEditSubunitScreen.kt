package es.pedrazamiguez.expenseshareapp.features.group.presentation.screen

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardNavigationBar
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardNavigationBarConfig
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard.WizardStepIndicator
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.SharedTransitionSurface
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.subunit.SubunitMembersStep
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.subunit.SubunitNameStep
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.subunit.SubunitReviewStep
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.subunit.SubunitSharesStep
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateEditSubunitUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateEditSubunitStep
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateEditSubunitUiState

/**
 * Shared element transition key for the Create Subunit FAB -> Screen transition.
 */
const val CREATE_EDIT_SUBUNIT_SHARED_ELEMENT_KEY = "create_edit_subunit_container"

@Composable
fun CreateEditSubunitScreen(
    uiState: CreateEditSubunitUiState = CreateEditSubunitUiState(),
    onEvent: (CreateEditSubunitUiEvent) -> Unit = {}
) {
    SharedTransitionSurface(sharedElementKey = CREATE_EDIT_SUBUNIT_SHARED_ELEMENT_KEY) {
        if (uiState.isLoading) {
            ShimmerLoadingList()
        } else {
            SubunitWizard(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun SubunitWizard(
    uiState: CreateEditSubunitUiState,
    onEvent: (CreateEditSubunitUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val stepLabelMap = rememberStepLabelMap()
    val orderedLabels = remember(uiState.steps, stepLabelMap) {
        uiState.steps.map { stepLabelMap[it] ?: "" }
    }

    val backLabel = stringResource(R.string.subunit_wizard_back)
    val nextLabel = stringResource(R.string.subunit_wizard_next)
    val submitLabel = stringResource(R.string.subunit_save)

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

            SubunitWizardStepContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.weight(1f)
            )

            WizardNavigationBar(
                config = WizardNavigationBarConfig(
                    canGoNext = uiState.canGoNext,
                    isOnLastStep = uiState.isOnReviewStep,
                    isCurrentStepValid = uiState.isCurrentStepValid,
                    isLoading = uiState.isSaving,
                    backLabel = backLabel,
                    nextLabel = nextLabel,
                    submitLabel = submitLabel
                ),
                onBack = { onEvent(CreateEditSubunitUiEvent.PreviousStep) },
                onNext = { onEvent(CreateEditSubunitUiEvent.NextStep) },
                onSubmit = { onEvent(CreateEditSubunitUiEvent.Save) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SubunitWizardStepContent(
    uiState: CreateEditSubunitUiState,
    onEvent: (CreateEditSubunitUiEvent) -> Unit,
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
        label = "subunitWizardStep"
    ) { step ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (step) {
                CreateEditSubunitStep.NAME -> SubunitNameStep(uiState = uiState, onEvent = onEvent)
                CreateEditSubunitStep.MEMBERS -> SubunitMembersStep(uiState = uiState, onEvent = onEvent)
                CreateEditSubunitStep.SHARES -> SubunitSharesStep(uiState = uiState, onEvent = onEvent)
                CreateEditSubunitStep.REVIEW -> SubunitReviewStep(uiState = uiState)
            }
        }
    }
}

@Composable
private fun rememberStepLabelMap(): Map<CreateEditSubunitStep, String> {
    val nameLabel = stringResource(R.string.subunit_wizard_step_name)
    val membersLabel = stringResource(R.string.subunit_wizard_step_members)
    val sharesLabel = stringResource(R.string.subunit_wizard_step_shares)
    val reviewLabel = stringResource(R.string.subunit_wizard_step_review)
    return remember(nameLabel, membersLabel, sharesLabel, reviewLabel) {
        mapOf(
            CreateEditSubunitStep.NAME to nameLabel,
            CreateEditSubunitStep.MEMBERS to membersLabel,
            CreateEditSubunitStep.SHARES to sharesLabel,
            CreateEditSubunitStep.REVIEW to reviewLabel
        )
    }
}
