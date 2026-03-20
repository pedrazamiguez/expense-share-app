package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitTypeUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import kotlinx.collections.immutable.ImmutableList

/**
 * Displays entity-level split rows for sub-unit mode (Level 1).
 *
 * Each entity row is either a solo user or a sub-unit header.
 * Sub-unit headers are expandable accordions that reveal intra-sub-unit splits (Level 2).
 *
 * @param entitySplits The entity-level split rows (solo users + sub-unit headers).
 * @param isEqualMode Whether the Level 1 split type is EQUAL.
 * @param isPercentMode Whether the Level 1 split type is PERCENT.
 * @param onAmountChanged Level 1 entity amount changed (EXACT mode).
 * @param onPercentageChanged Level 1 entity percentage changed (PERCENT mode).
 * @param onExcludedToggled Level 1 entity exclude toggle.
 * @param onShareLockToggled Level 1 entity share lock toggle.
 * @param onAccordionToggled Toggles sub-unit accordion expansion.
 * @param onIntraSubunitSplitTypeChanged Level 2 per-sub-unit split type changed.
 * @param onIntraSubunitAmountChanged Level 2 intra-sub-unit member amount changed.
 * @param onIntraSubunitPercentageChanged Level 2 intra-sub-unit member percentage changed.
 * @param onIntraSubunitShareLockToggled Level 2 intra-sub-unit member share lock toggle.
 */
@Composable
fun EntitySplitEditor(
    entitySplits: ImmutableList<SplitUiModel>,
    isEqualMode: Boolean,
    isPercentMode: Boolean,
    availableSplitTypes: ImmutableList<SplitTypeUiModel>,
    onAmountChanged: (entityId: String, amount: String) -> Unit,
    onPercentageChanged: (entityId: String, percentage: String) -> Unit,
    onExcludedToggled: (entityId: String) -> Unit,
    onShareLockToggled: (entityId: String) -> Unit,
    onAccordionToggled: (entityId: String) -> Unit,
    onIntraSubunitSplitTypeChanged: (subunitId: String, splitTypeId: String) -> Unit,
    onIntraSubunitAmountChanged: (subunitId: String, userId: String, amount: String) -> Unit,
    onIntraSubunitPercentageChanged: (subunitId: String, userId: String, percentage: String) -> Unit,
    onIntraSubunitShareLockToggled: (subunitId: String, userId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entitySplits.forEach { entity ->
            EntitySplitRow(
                entity = entity,
                isEqualMode = isEqualMode,
                isPercentMode = isPercentMode,
                availableSplitTypes = availableSplitTypes,
                onAmountChanged = { amount -> onAmountChanged(entity.userId, amount) },
                onPercentageChanged = { pct -> onPercentageChanged(entity.userId, pct) },
                onExcludedToggled = { onExcludedToggled(entity.userId) },
                onShareLockToggled = { onShareLockToggled(entity.userId) },
                onAccordionToggled = { onAccordionToggled(entity.userId) },
                onIntraSubunitSplitTypeChanged = { splitTypeId ->
                    onIntraSubunitSplitTypeChanged(entity.userId, splitTypeId)
                },
                onIntraSubunitAmountChanged = { userId, amount ->
                    onIntraSubunitAmountChanged(entity.userId, userId, amount)
                },
                onIntraSubunitPercentageChanged = { userId, pct ->
                    onIntraSubunitPercentageChanged(entity.userId, userId, pct)
                },
                onIntraSubunitShareLockToggled = { userId ->
                    onIntraSubunitShareLockToggled(entity.userId, userId)
                },
                onDone = { focusManager.clearFocus() }
            )
        }
    }
}

/**
 * A single entity row — either a solo user or an expandable sub-unit header.
 *
 * Sub-unit rows display a group icon and expand/collapse chevron.
 * When expanded, they reveal the [IntraSubunitSplitEditor] for Level 2 splitting.
 */
@Composable
private fun EntitySplitRow(
    entity: SplitUiModel,
    isEqualMode: Boolean,
    isPercentMode: Boolean,
    availableSplitTypes: ImmutableList<SplitTypeUiModel>,
    onAmountChanged: (String) -> Unit,
    onPercentageChanged: (String) -> Unit,
    onExcludedToggled: () -> Unit,
    onShareLockToggled: () -> Unit,
    onAccordionToggled: () -> Unit,
    onIntraSubunitSplitTypeChanged: (String) -> Unit,
    onIntraSubunitAmountChanged: (userId: String, amount: String) -> Unit,
    onIntraSubunitPercentageChanged: (userId: String, percentage: String) -> Unit,
    onIntraSubunitShareLockToggled: (userId: String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasMembers = entity.entityMembers.isNotEmpty()
    val isSubunitHeader = entity.isEntityRow && hasMembers

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Entity header row ────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSubunitHeader) {
                        Modifier
                            .clip(MaterialTheme.shapes.large)
                            .clickable { onAccordionToggled() }
                    } else {
                        Modifier
                    }
                )
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sub-unit group icon
            if (isSubunitHeader) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = if (entity.isExcluded) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            // Entity name + optional secondary amount text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSubunitHeader) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (entity.isExcluded) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                // Show sub-unit member count
                if (isSubunitHeader && !entity.isExcluded) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.add_expense_split_subunit_members_count,
                            entity.entityMembers.size,
                            entity.entityMembers.size
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Show currency amount as secondary text for EXACT and PERCENT modes
                if (!entity.isExcluded && !isEqualMode && entity.formattedAmount.isNotBlank()) {
                    Text(
                        text = entity.formattedAmount,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Amount / percentage input (same pattern as flat SplitMemberRow)
            AnimatedVisibility(visible = !entity.isExcluded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isEqualMode) {
                        Text(
                            text = entity.formattedAmount,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (isPercentMode) {
                        StyledOutlinedTextField(
                            value = entity.percentageInput,
                            onValueChange = onPercentageChanged,
                            label = stringResource(R.string.add_expense_split_percentage_label),
                            modifier = Modifier.widthIn(max = 100.dp),
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next,
                            keyboardActions = KeyboardActions(onNext = { onDone() })
                        )
                        ShareLockIcon(isLocked = entity.isShareLocked, onClick = onShareLockToggled)
                    } else {
                        // EXACT mode
                        StyledOutlinedTextField(
                            value = entity.amountInput,
                            onValueChange = onAmountChanged,
                            label = stringResource(R.string.add_expense_split_amount_label),
                            modifier = Modifier.widthIn(max = 120.dp),
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next,
                            keyboardActions = KeyboardActions(onNext = { onDone() })
                        )
                        ShareLockIcon(isLocked = entity.isShareLocked, onClick = onShareLockToggled)
                    }
                }
            }

            // Accordion chevron for sub-unit headers
            if (isSubunitHeader) {
                Icon(
                    imageVector = if (entity.isExpanded) {
                        Icons.Default.ExpandLess
                    } else {
                        Icons.Default.ExpandMore
                    },
                    contentDescription = stringResource(
                        if (entity.isExpanded) {
                            R.string.add_expense_split_subunit_collapse
                        } else {
                            R.string.add_expense_split_subunit_expand
                        }
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Exclude toggle
            Switch(
                checked = !entity.isExcluded,
                onCheckedChange = { onExcludedToggled() }
            )
        }

        // ── Accordion content: Intra-sub-unit splits ─────────────
        if (isSubunitHeader) {
            AnimatedVisibility(
                visible = entity.isExpanded && !entity.isExcluded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 4.dp, bottom = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    IntraSubunitSplitEditor(
                        members = entity.entityMembers,
                        entitySplitType = entity.entitySplitType,
                        availableSplitTypes = availableSplitTypes,
                        onSplitTypeChanged = onIntraSubunitSplitTypeChanged,
                        onAmountChanged = onIntraSubunitAmountChanged,
                        onPercentageChanged = onIntraSubunitPercentageChanged,
                        onShareLockToggled = onIntraSubunitShareLockToggled,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
