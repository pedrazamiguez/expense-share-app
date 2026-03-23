package es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.subunit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.form.FormErrorBanner
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.MemberUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateEditSubunitUiState

/**
 * Step 4: Read-only summary of the subunit before saving.
 */
@Composable
fun SubunitReviewStep(
    uiState: CreateEditSubunitUiState,
    modifier: Modifier = Modifier
) {
    val none = stringResource(R.string.subunit_review_none)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.subunit_review_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        ReviewCard(uiState = uiState, none = none)

        FormErrorBanner(error = uiState.nameError)
        FormErrorBanner(error = uiState.membersError)
        FormErrorBanner(error = uiState.sharesError)
    }
}

@Composable
private fun ReviewCard(uiState: CreateEditSubunitUiState, none: String) {
    val memberMap = remember(uiState.availableMembers) {
        uiState.availableMembers.associateBy { it.userId }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReviewRow(
                label = stringResource(R.string.subunit_review_name),
                value = uiState.name.ifBlank { none }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ReviewRow(
                label = stringResource(R.string.subunit_review_members),
                value = uiState.selectedMemberIds
                    .mapNotNull { memberMap[it] }
                    .joinToString { it.displayName }
                    .ifBlank { none }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ReviewSharesList(uiState = uiState, memberMap = memberMap, none = none)
        }
    }
}

@Composable
private fun ReviewSharesList(
    uiState: CreateEditSubunitUiState,
    memberMap: Map<String, MemberUiModel>,
    none: String
) {
    val sharesLabel = stringResource(R.string.subunit_review_shares)
    val selectedMembers = uiState.selectedMemberIds.mapNotNull { memberMap[it] }

    if (selectedMembers.isEmpty()) {
        ReviewRow(label = sharesLabel, value = none)
    } else {
        Text(
            text = sharesLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        selectedMembers.forEach { member ->
            val shareText = uiState.memberShares[member.userId]
            val displayValue = if (shareText.isNullOrBlank()) none else "$shareText%"
            ReviewRow(label = member.displayName, value = displayValue)
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
