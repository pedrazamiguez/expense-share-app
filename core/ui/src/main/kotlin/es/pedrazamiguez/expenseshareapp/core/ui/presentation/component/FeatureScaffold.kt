package es.pedrazamiguez.expenseshareapp.core.ui.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.screen.ScreenUiProvider
import org.koin.compose.getKoin

@Composable
fun FeatureScaffold(
    modifier: Modifier = Modifier, content: @Composable () -> Unit
) {

    val navController = LocalRootNavController.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val koin = getKoin()
    val providers = remember(koin) { koin.getAll<ScreenUiProvider>() }

    val currentProvider = remember(
        currentRoute,
        providers
    ) {
        providers.find { it.route == currentRoute }
    }

    Scaffold(
        modifier = modifier,
        topBar = { currentProvider?.topBar?.invoke() },
        floatingActionButton = { currentProvider?.fab?.invoke() }) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            content()
        }
    }

}
