package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.SharedTransitionSurface
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.component.PayerTypeScopeCard
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.component.PayerTypeScopeCardLabels
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddContributionUiEvent
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

    val focusManager = LocalFocusManager.current

    val submitForm = {
        focusManager.clearFocus()
        if (!uiState.isLoading) {
            onEvent(AddContributionUiEvent.Submit(groupId))
        }
    }

    SharedTransitionSurface(sharedElementKey = ADD_CONTRIBUTION_SHARED_ELEMENT_KEY) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ContributionAmountCard(uiState = uiState, onEvent = onEvent, submitForm = submitForm)
                    ContributionScopeCard(uiState = uiState, onEvent = onEvent)
                }

                ContributionSubmitButton(
                    isLoading = uiState.isLoading,
                    onSubmit = submitForm
                )
            }
        }
    }
}

@Composable
private fun ContributionAmountCard(
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit,
    submitForm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StyledOutlinedTextField(
                value = uiState.amountInput,
                onValueChange = { onEvent(AddContributionUiEvent.UpdateAmount(it)) },
                label = stringResource(R.string.balances_add_money_amount_hint),
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Decimal,
                isError = uiState.amountError,
                supportingText = if (uiState.amountError) {
                    stringResource(R.string.balances_add_money_error_amount)
                } else {
                    null
                },
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { submitForm() })
            )
        }
    }
}

@Composable
private fun ContributionScopeCard(
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit
) {
    PayerTypeScopeCard(
        labels = PayerTypeScopeCardLabels(
            title = stringResource(R.string.balances_add_money_contributing_for),
            groupLabel = stringResource(R.string.balances_add_money_for_group),
            personalLabel = stringResource(R.string.balances_add_money_for_me),
            subunitLabelTemplate = stringResource(R.string.balances_add_money_for_subunit)
        ),
        selectedScope = uiState.contributionScope,
        selectedSubunitId = uiState.selectedSubunitId,
        subunitOptions = uiState.subunitOptions,
        onScopeSelected = { scope, subunitId ->
            onEvent(AddContributionUiEvent.ContributionScopeSelected(scope, subunitId))
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContributionSubmitButton(isLoading: Boolean, onSubmit: () -> Unit) {
    val bottomNavPadding = LocalBottomPadding.current
    val isKeyboardVisible = WindowInsets.isImeVisible
    val effectiveBottomPadding = if (isKeyboardVisible) 12.dp else 12.dp + bottomNavPadding

    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = effectiveBottomPadding)
                .height(56.dp),
            enabled = !isLoading,
            shape = MaterialTheme.shapes.large
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.balances_add_money_submit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
