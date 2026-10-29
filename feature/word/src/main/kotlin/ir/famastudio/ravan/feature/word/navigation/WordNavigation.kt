package ir.famastudio.ravan.feature.word.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import ir.famastudio.ravan.feature.word.WordDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data class WordDetailRoute(val wordId: String)

fun NavController.navigateToWord(wordId: String, navOptions: NavOptions? = null) =
    navigate(WordDetailRoute(wordId), navOptions)

fun NavGraphBuilder.wordDetailScreen(
    onBack: () -> Unit,
    onWordClick: (wordId: String) -> Unit,
    onAddWord: (initialText: String) -> Unit,
    onEditWord: (wordId: String) -> Unit,
) {
    composable<WordDetailRoute> {
        WordDetailScreen(onBack = onBack, onWordClick = onWordClick, onAddWord = onAddWord, onEditWord = onEditWord)
    }
}
