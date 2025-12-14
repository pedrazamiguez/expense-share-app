package es.pedrazamiguez.expenseshareapp.features.group.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.theme.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import java.time.LocalDateTime

@Composable
fun GroupCard(
    group: Group, isSelected: Boolean = false, onClick: (String) -> Unit = { _ -> }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(group.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = group.currency,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun GroupCardPreview() {
    ExpenseShareAppTheme {
        GroupCard(
            group = Group(
                name = "Thai 2.0", createdAt = LocalDateTime.now()
            )
        )
    }
}
