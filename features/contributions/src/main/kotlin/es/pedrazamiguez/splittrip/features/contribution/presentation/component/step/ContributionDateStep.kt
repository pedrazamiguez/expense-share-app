package es.pedrazamiguez.splittrip.features.contribution.presentation.component.step

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.AppDateTimeSelectionSection
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.splittrip.features.contribution.R
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.event.AddContributionUiEvent
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.state.AddContributionUiState

@Composable
fun ContributionDateStep(
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        AppDateTimeSelectionSection(
            title = stringResource(R.string.contribution_date_time_title),
            label = stringResource(R.string.contribution_date_time_label),
            formattedDateTime = uiState.formattedContributionDate,
            isDateTimeValid = true,
            dateTimeMillis = uiState.contributionDateMillis,
            onDateTimeSelected = { onEvent(AddContributionUiEvent.ContributionDateChanged(it)) }
        )
    }
}
