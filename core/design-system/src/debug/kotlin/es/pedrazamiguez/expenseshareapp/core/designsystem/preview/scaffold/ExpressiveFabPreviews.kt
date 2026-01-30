package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold.ExpressiveFab
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemes

@PreviewThemes
@Composable
private fun ExpressiveFabPreview() {
    PreviewThemeWrapper {
        ExpressiveFab(
            onClick = {}, icon = Icons.Default.Add, contentDescription = "Add"
        )
    }
}

@PreviewThemes
@Composable
private fun ExpressiveFabFullScreenPreview() {
    PreviewThemeWrapper {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), contentAlignment = Alignment.BottomEnd
        ) {
            ExpressiveFab(
                onClick = {}, icon = Icons.Default.Add, contentDescription = "Add"
            )
        }
    }
}
