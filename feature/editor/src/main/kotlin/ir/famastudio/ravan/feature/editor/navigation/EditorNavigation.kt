package ir.famastudio.ravan.feature.editor.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import ir.famastudio.ravan.feature.editor.WordEditorScreen
import kotlinx.serialization.Serializable

/** `wordId == null` creates a new word, optionally pre-filled with [initialText]. */
@Serializable
data class WordEditorRoute(
    val wordId: String? = null,
    val initialText: String? = null,
)

fun NavController.navigateToWordEditor(
    wordId: String? = null,
    initialText: String? = null,
    navOptions: NavOptions? = null,
) = navigate(WordEditorRoute(wordId, initialText?.takeIf { it.isNotBlank() }), navOptions)

fun NavGraphBuilder.wordEditorScreen(
    onClose: () -> Unit,
    onSaved: (wordId: String, isNew: Boolean) -> Unit,
    onOpenWord: (wordId: String) -> Unit,
) {
    composable<WordEditorRoute> {
        WordEditorScreen(onClose = onClose, onSaved = onSaved, onOpenWord = onOpenWord)
    }
}
