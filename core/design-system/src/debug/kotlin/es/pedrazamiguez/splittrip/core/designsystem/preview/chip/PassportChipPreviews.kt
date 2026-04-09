package es.pedrazamiguez.splittrip.core.designsystem.preview.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.chip.PassportChip
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemes

@PreviewThemes
@Composable
private fun PassportChipSelectedUnselectedPreview() {
    PreviewThemeWrapper {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PassportChip(
                label = "Unselected",
                selected = false,
                onClick = {}
            )
            PassportChip(
                label = "Selected",
                selected = true,
                onClick = {},
                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
            )
        }
    }
}

@PreviewThemes
@Composable
private fun PassportChipVariantsPreview() {
    PreviewThemeWrapper {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PassportChip(label = "Food", selected = false, onClick = {})
                PassportChip(label = "Transport", selected = false, onClick = {})
                PassportChip(
                    label = "Lodging",
                    selected = true,
                    onClick = {},
                    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                )
            }
            // Removable chip — SearchableChipSelector use case
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PassportChip(
                    label = "Alice",
                    selected = true,
                    onClick = {},
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                PassportChip(
                    label = "Bob",
                    selected = true,
                    onClick = {},
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            // Overflow chip — CondensedChips use case
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PassportChip(label = "Groceries", selected = false, onClick = {})
                PassportChip(
                    label = "+ More",
                    selected = false,
                    onClick = {},
                    trailingIcon = { Icon(Icons.Default.MoreHoriz, contentDescription = null) }
                )
            }
        }
    }
}
