package es.pedrazamiguez.expenseshareapp.ui.group.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.ui.group.R
import java.time.format.DateTimeFormatter

@Composable
fun GroupCard(
    group: Group,
    isSelected: Boolean = false,
    onClick: (String) -> Unit = { _ -> }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(group.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = group.name)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${stringResource(R.string.group_field_currency)}: ${group.currency}")
            group.createdAt?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${stringResource(R.string.group_field_created_at)}: ${it.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}"
                )
            }
        }
    }
}
