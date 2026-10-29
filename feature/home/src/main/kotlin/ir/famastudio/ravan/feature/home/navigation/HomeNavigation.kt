package ir.famastudio.ravan.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import ir.famastudio.ravan.feature.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(HomeRoute, navOptions)

fun NavGraphBuilder.homeScreen(
    onWordClick: (wordId: String) -> Unit,
    onAddWord: (initialText: String) -> Unit,
    onStartReview: () -> Unit,
) {
    composable<HomeRoute> {
        HomeScreen(onWordClick = onWordClick, onAddWord = onAddWord, onStartReview = onStartReview)
    }
}
