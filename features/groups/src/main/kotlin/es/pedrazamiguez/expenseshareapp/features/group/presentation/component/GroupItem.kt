package es.pedrazamiguez.expenseshareapp.features.group.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.MappedPreview
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel

@Composable
fun GroupItem(
    groupUiModel: GroupUiModel,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (String) -> Unit = { _ -> }
) {
    Card(
        onClick = { onClick(groupUiModel.id) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = groupUiModel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = groupUiModel.currency,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (groupUiModel.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = groupUiModel.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = groupUiModel.membersCountText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (groupUiModel.dateText.isNotEmpty()) {
                    Text(
                        text = groupUiModel.dateText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@PreviewComplete
@Composable
private fun GroupItemPreview() {
    MappedPreview(

    ) { }
    PreviewThemeWrapper {
        GroupItem(
            groupUiModel = GroupUiModel(
                id = "1",
                name = "Thai 2.0",
                description = "Trip to Thailand with friends",
                currency = "EUR",
                membersCountText = "3 members",
                dateText = "25 Jan"
            )
        )
    }
}

@Preview
@Composable
private fun GroupItemSelectedPreview() {
    ExpenseShareAppTheme {
        GroupItem(
            groupUiModel = GroupUiModel(
                id = "1",
                name = "Thai 2.0",
                description = "Trip to Thailand with friends",
                currency = "EUR",
                membersCountText = "3 members",
                dateText = "25 Jan"
            ),
            isSelected = true
        )
    }
}

