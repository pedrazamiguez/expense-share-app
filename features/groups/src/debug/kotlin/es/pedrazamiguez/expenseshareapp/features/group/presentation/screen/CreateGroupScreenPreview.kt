package es.pedrazamiguez.expenseshareapp.features.group.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.CreateGroupUiState

@Preview
@Composable
fun CreateGroupScreenPreview() {
    CreateGroupScreen(
        uiState = CreateGroupUiState()
    )
}
