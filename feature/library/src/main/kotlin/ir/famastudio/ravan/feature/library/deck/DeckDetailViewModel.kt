package ir.famastudio.ravan.feature.library.deck

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.famastudio.ravan.core.data.repository.DeckRepository
import ir.famastudio.ravan.core.data.repository.ReviewRepository
import ir.famastudio.ravan.core.data.repository.WordRepository
import ir.famastudio.ravan.core.model.Deck
import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.core.model.WordFilter
import ir.famastudio.ravan.feature.library.navigation.DeckDetailRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DeckDetailUiState {
    data object Loading : DeckDetailUiState
    data object Deleted : DeckDetailUiState
    data class Success(val deck: Deck, val words: List<Word>) : DeckDetailUiState
}

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deckRepository: DeckRepository,
    private val reviewRepository: ReviewRepository,
    wordRepository: WordRepository,
) : ViewModel() {

    private val deckId = savedStateHandle.toRoute<DeckDetailRoute>().deckId

    val uiState: StateFlow<DeckDetailUiState> = combine(
        deckRepository.observeDeck(deckId),
        deckRepository.observeDeckWords(deckId),
    ) { deck, words ->
        if (deck == null) DeckDetailUiState.Deleted else DeckDetailUiState.Success(deck, words)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DeckDetailUiState.Loading)

    private val pickerQuery = MutableStateFlow("")
    val query: StateFlow<String> = pickerQuery.asStateFlow()

    /** Candidate words for the picker: search results, or recently viewed words when the query is blank. */
    val pickerWords: StateFlow<List<Word>> = pickerQuery
        .flatMapLatest { q -> if (q.isBlank()) wordRepository.observeWords(WordFilter.RECENT) else wordRepository.search(q, limit = 200) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<Boolean>(false)
    /** True once "add all to review" finished — shown as a snackbar. */
    val addedToReview: StateFlow<Boolean> = _message.asStateFlow()

    fun onQueryChange(value: String) {
        pickerQuery.value = value
    }

    fun rename(name: String, colorIndex: Int) {
        viewModelScope.launch { deckRepository.updateDeck(deckId, name, colorIndex) }
    }

    fun delete() {
        viewModelScope.launch { deckRepository.deleteDeck(deckId) }
    }

    fun removeWord(wordId: String) {
        viewModelScope.launch { deckRepository.removeWordFromDeck(deckId, wordId) }
    }

    fun addWords(wordIds: Set<String>) {
        viewModelScope.launch { deckRepository.addWordsToDeck(deckId, wordIds) }
    }

    fun addAllToReview() {
        val state = uiState.value as? DeckDetailUiState.Success ?: return
        viewModelScope.launch {
            reviewRepository.addAllToReview(state.words.map { it.id })
            _message.value = true
        }
    }

    fun onMessageShown() {
        _message.value = false
    }
}
