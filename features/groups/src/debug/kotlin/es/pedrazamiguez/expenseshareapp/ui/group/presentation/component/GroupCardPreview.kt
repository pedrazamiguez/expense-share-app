package es.pedrazamiguez.expenseshareapp.ui.group.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import java.time.LocalDateTime

@Preview
@Composable
private fun GroupCardPreview() {
    GroupCard(
        group = Group(
            name = "Putivuelta Yucateca",
            createdAt = LocalDateTime.now()
        )
    )
}
