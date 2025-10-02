package es.pedrazamiguez.expenseshareapp.ui.group.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature.CreateGroupFeature
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature.GroupsFeature
import org.koin.core.context.GlobalContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.groupsGraph() {

    composable(route = Routes.GROUPS_ROOT) {

        val innerNavController = rememberNavController()

        val koin = remember { GlobalContext.get() }
        val screenUiProviders = remember { koin.getAll<ScreenUiProvider>() }

        CompositionLocalProvider(LocalTabNavController provides innerNavController) {

            val navBackStackEntry = innerNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry.value?.destination?.route

            val provider = screenUiProviders.firstOrNull { it.route == currentRoute }

            Scaffold(
                topBar = { provider?.topBar?.invoke() },
                floatingActionButton = { provider?.fab?.invoke() }) { _ ->
                NavHost(
                    navController = innerNavController,
                    startDestination = Routes.GROUPS,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Routes.GROUPS) {
                        GroupsFeature()
                    }
                    composable(Routes.CREATE_GROUP) {
                        CreateGroupFeature()
                    }
                }
            }

        }

    }

}
