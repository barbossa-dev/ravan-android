package ir.famastudio.ravan.feature.home

import ir.famastudio.ravan.core.model.Accent
import ir.famastudio.ravan.core.model.Word

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data class Results(val query: String, val words: List<Word>) : SearchUiState
}

sealed interface HomeFeedUiState {
    data object Loading : HomeFeedUiState
    data class Success(
        val wordOfTheDay: Word?,
        val recentWords: List<Word>,
        val dueCount: Int,
        val wordCount: Int,
        val accent: Accent,
    ) : HomeFeedUiState
}
