package pe.edu.upc.viora

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import pe.edu.upc.viora.core.designsystem.component.TabBarItem
import pe.edu.upc.viora.core.designsystem.component.VioraTabBar
import pe.edu.upc.viora.core.designsystem.theme.Spacing
import pe.edu.upc.viora.core.navigation.TopLevelDestination
import pe.edu.upc.viora.navigation.AppNavHost

/** App shell: navigation host plus the producer's floating tab bar on top-level screens. */
@Composable
fun VioraApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    val tabs = TopLevelDestination.entries
    val selectedIndex = tabs.indexOfFirst { tab ->
        currentDestination?.hierarchy?.any { it.hasRoute(tab.graph::class) } == true
    }
    // Only tab roots show the bar; detail screens (e.g. plot detail) take the full screen.
    val showTabBar = tabs.any { currentDestination?.hasRoute(it.startRoute) == true }

    val items = tabs.map { TabBarItem(icon = it.icon, label = stringResource(it.label)) }
    val actionLabel = stringResource(R.string.nav_action_add)

    // No insets here: each screen decides how to use the area under the system bars (maps go
    // beneath them, lists pad themselves), so the status bar can stay transparent.
    Scaffold(modifier = modifier.fillMaxSize(), contentWindowInsets = WindowInsets(0, 0, 0, 0)) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            AppNavHost(navController = navController)

            AnimatedVisibility(
                visible = showTabBar,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                VioraTabBar(
                    items = items,
                    selectedIndex = selectedIndex.coerceAtLeast(0),
                    onItemClick = { index ->
                        navController.navigate(tabs[index].graph) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    actionLabel = actionLabel,
                    // Opens the "what will you record?" menu once the logging features exist.
                    onActionClick = {},
                    modifier = Modifier.navigationBarsPadding().padding(bottom = Spacing.sm),
                )
            }
        }
    }
}
