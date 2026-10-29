package ir.famastudio.ravan.feature.library.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import ir.famastudio.ravan.feature.library.LibraryScreen
import ir.famastudio.ravan.feature.library.deck.DeckDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data object LibraryRoute

@Serializable
data class DeckDetailRoute(val deckId: String)

fun NavController.navigateToLibrary(navOptions: NavOptions? = null) = navigate(LibraryRoute, navOptions)

fun NavController.navigateToDeck(deckId: String) = navigate(DeckDetailRoute(deckId))

fun NavGraphBuilder.libraryScreen(
    onWordClick: (String) -> Unit,
    onAddWord: () -> Unit,
    onDeckClick: (String) -> Unit,
) {
    composable<LibraryRoute> {
        LibraryScreen(onWordClick = onWordClick, onAddWord = onAddWord, onDeckClick = onDeckClick)
    }
}

fun NavGraphBuilder.deckDetailScreen(
    onBack: () -> Unit,
    onWordClick: (String) -> Unit,
    onReviewDeck: (deckId: String) -> Unit,
) {
    composable<DeckDetailRoute> {
        DeckDetailScreen(onBack = onBack, onWordClick = onWordClick, onReviewDeck = onReviewDeck)
    }
}
