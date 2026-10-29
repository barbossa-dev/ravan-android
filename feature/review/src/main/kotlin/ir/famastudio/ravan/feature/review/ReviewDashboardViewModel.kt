package ir.famastudio.ravan.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.famastudio.ravan.core.data.repository.DeckRepository
import ir.famastudio.ravan.core.domain.GetReviewStatsUseCase
import ir.famastudio.ravan.core.model.Deck
import ir.famastudio.ravan.core.model.ReviewStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ReviewDashboardUiState {
    data object Loading : ReviewDashboardUiState
    data class Success(val stats: ReviewStats, val decks: List<Deck>) : ReviewDashboardUiState
}

@HiltViewModel
class ReviewDashboardViewModel @Inject constructor(
    getReviewStats: GetReviewStatsUseCase,
    deckRepository: DeckRepository,
) : ViewModel() {
    val uiState: StateFlow<ReviewDashboardUiState> = combine(getReviewStats(), deckRepository.observeDecks()) { stats, decks ->
        ReviewDashboardUiState.Success(stats, decks.filter { it.wordCount > 0 })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReviewDashboardUiState.Loading)
}
