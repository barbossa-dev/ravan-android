package ir.famastudio.ravan.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.navigation.RavanNavHost
import ir.famastudio.ravan.navigation.TopLevelDestination

/**
 * App shell: a bottom navigation bar on phones, a navigation rail on tablets / foldables,
 * hidden on detail screens (word, editor, review session).
 */
@Composable
fun RavanApp(
    dueCount: Int,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentTopLevel = TopLevelDestination.entries.firstOrNull { currentDestination.isOn(it) }
    val showNavigation = currentDestination == null || currentTopLevel != null
    val useRail = LocalConfiguration.current.screenWidthDp >= 600

    val navigateToTopLevel: (TopLevelDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Row(Modifier.fillMaxSize()) {
        if (useRail && showNavigation) {
            NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationRailItem(
                        selected = destination == currentTopLevel,
                        onClick = { navigateToTopLevel(destination) },
                        icon = { DestinationIcon(destination, dueCount) },
                        label = { Text(stringResource(destination.label)) },
                    )
                }
            }
        }
        Scaffold(
            modifier = Modifier.weight(1f),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!useRail && showNavigation) {
                    NavigationBar {
                        TopLevelDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = destination == currentTopLevel,
                                onClick = { navigateToTopLevel(destination) },
                                icon = { DestinationIcon(destination, dueCount) },
                                label = { Text(stringResource(destination.label)) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            RavanNavHost(
                navController = navController,
                onNavigateToTopLevel = navigateToTopLevel,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
            )
        }
    }
}

@Composable
private fun DestinationIcon(destination: TopLevelDestination, dueCount: Int) {
    if (destination == TopLevelDestination.REVIEW && dueCount > 0) {
        BadgedBox(badge = { Badge { Text(dueCount.coerceAtMost(99).toPersianDigits()) } }) {
            Icon(destination.icon, contentDescription = null)
        }
    } else {
        Icon(destination.icon, contentDescription = null)
    }
}

private fun NavDestination?.isOn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
