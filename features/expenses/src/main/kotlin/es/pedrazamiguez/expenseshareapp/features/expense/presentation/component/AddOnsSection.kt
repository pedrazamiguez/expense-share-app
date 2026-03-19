package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Add-ons section of the Add Expense form.
 *
 * Displays a collapsible section with individual add-on editors,
 * an "Add" button for new add-ons, the effective total display,
 * and any add-on validation error.
 *
 * Stateless: takes [AddExpenseUiState] and emits [AddExpenseUiEvent]s.
 */
@Composable
fun AddOnsSection(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    val hasAddOns = uiState.addOns.isNotEmpty()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AddOnsSectionHeader(
            hasAddOns = hasAddOns,
            isExpanded = uiState.isAddOnsSectionExpanded,
            onToggle = { onEvent(AddExpenseUiEvent.AddOnsSectionToggled) }
        )

        AddOnsListCard(
            uiState = uiState,
            onEvent = onEvent,
            focusManager = focusManager,
            isVisible = uiState.isAddOnsSectionExpanded && hasAddOns
        )

        AddOnsSectionFooter(
            effectiveTotal = uiState.effectiveTotal,
            addOnError = uiState.addOnError,
            onAddClicked = { onEvent(AddExpenseUiEvent.AddOnAdded(AddOnType.FEE)) }
        )
    }
}

@Composable
private fun AddOnsSectionHeader(
    hasAddOns: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.add_expense_add_ons_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (hasAddOns) {
            TextButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.ExpandLess
                    } else {
                        Icons.Default.ExpandMore
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AddOnsListCard(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    focusManager: FocusManager,
    isVisible: Boolean
) {
    val showCurrencySelector = uiState.availableCurrencies.size > 1

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.addOns.forEachIndexed { index, addOn ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    AddOnItemEditor(
                        addOn = addOn,
                        availableCurrencies = uiState.availableCurrencies,
                        paymentMethods = uiState.paymentMethods,
                        showCurrencySelector = showCurrencySelector,
                        focusManager = focusManager,
                        onEvent = { event -> onEvent(event) },
                        onRemove = {
                            onEvent(AddExpenseUiEvent.AddOnRemoved(addOn.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddOnsSectionFooter(
    effectiveTotal: String,
    addOnError: UiText?,
    onAddClicked: () -> Unit
) {
    if (effectiveTotal.isNotBlank()) {
        Text(
            text = stringResource(
                R.string.add_expense_add_on_effective_total,
                effectiveTotal
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    addOnError?.let { errorUiText ->
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = errorUiText.asString(),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    TextButton(
        onClick = onAddClicked,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = stringResource(R.string.add_expense_add_on_button),
            style = MaterialTheme.typography.labelLarge
        )
    }
}
