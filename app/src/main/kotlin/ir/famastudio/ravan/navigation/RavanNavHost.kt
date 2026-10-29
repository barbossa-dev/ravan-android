package ir.famastudio.ravan.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import ir.famastudio.ravan.feature.editor.navigation.navigateToWordEditor
import ir.famastudio.ravan.feature.editor.navigation.wordEditorScreen
import ir.famastudio.ravan.feature.home.navigation.HomeRoute
import ir.famastudio.ravan.feature.home.navigation.homeScreen
import ir.famastudio.ravan.feature.library.navigation.deckDetailScreen
import ir.famastudio.ravan.feature.library.navigation.libraryScreen
import ir.famastudio.ravan.feature.library.navigation.navigateToDeck
import ir.famastudio.ravan.feature.review.navigation.navigateToReviewSession
import ir.famastudio.ravan.feature.review.navigation.reviewScreen
import ir.famastudio.ravan.feature.review.navigation.reviewSessionScreen
import ir.famastudio.ravan.feature.settings.navigation.settingsScreen
import ir.famastudio.ravan.feature.word.navigation.navigateToWord
import ir.famastudio.ravan.feature.word.navigation.wordDetailScreen

/**
 * The whole navigation graph. Features never know about each other: every cross-feature
 * jump is wired here, exactly like Now in Android.
 */
@Composable
fun RavanNavHost(
    navController: NavHostController,
    onNavigateToTopLevel: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val openWord: (String) -> Unit = { wordId -> navController.navigateToWord(wordId) }
    val addWord: (String) -> Unit = { text -> navController.navigateToWordEditor(initialText = text) }
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = HomeRoute, modifier = modifier) {
        homeScreen(
            onWordClick = openWord,
            onAddWord = addWord,
            onStartReview = { navController.navigateToReviewSession() },
        )
        wordDetailScreen(
            onBack = back,
            onWordClick = openWord,
            onAddWord = addWord,
            onEditWord = { wordId -> navController.navigateToWordEditor(wordId = wordId) },
        )
        wordEditorScreen(
            onClose = back,
            onSaved = { wordId, isNew ->
                navController.popBackStack()
                if (isNew) navController.navigateToWord(wordId)
            },
            onOpenWord = { wordId ->
                navController.popBackStack()
                navController.navigateToWord(wordId)
            },
        )
        libraryScreen(
            onWordClick = openWord,
            onAddWord = { navController.navigateToWordEditor() },
            onDeckClick = { deckId -> navController.navigateToDeck(deckId) },
        )
        deckDetailScreen(
            onBack = back,
            onWordClick = openWord,
            onReviewDeck = { deckId -> navController.navigateToReviewSession(deckId = deckId) },
        )
        reviewScreen(
            onStartSession = { deckId, practice -> navController.navigateToReviewSession(deckId, practice) },
            onBrowseDictionary = { onNavigateToTopLevel(TopLevelDestination.HOME) },
        )
        reviewSessionScreen(
            onClose = back,
            onWordClick = openWord,
        )
        settingsScreen()
    }
}
