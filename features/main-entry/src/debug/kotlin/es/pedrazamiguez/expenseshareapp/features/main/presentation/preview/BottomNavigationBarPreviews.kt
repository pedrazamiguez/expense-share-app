package es.pedrazamiguez.expenseshareapp.features.main.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewNavigationProviders
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.main.presentation.component.BottomNavigationBar

@PreviewComplete
@Composable
private fun BottomNavigationBarPreview() {
    PreviewThemeWrapper {
        BottomNavigationBar(
            selectedRoute = Routes.EXPENSES,
            items = PreviewNavigationProviders.full
        )
    }
}

@PreviewComplete
@Composable
private fun BottomNavigationBarWithTwoItemsPreview() {
    PreviewThemeWrapper {
        BottomNavigationBar(
            selectedRoute = Routes.GROUPS,
            items = PreviewNavigationProviders.minimal
        )
    }
}
