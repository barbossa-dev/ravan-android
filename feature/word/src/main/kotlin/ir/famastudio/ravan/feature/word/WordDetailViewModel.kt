package ir.famastudio.ravan.feature.word

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.famastudio.ravan.core.data.repository.DeckRepository
import ir.famastudio.ravan.core.data.repository.ReviewRepository
import ir.famastudio.ravan.core.data.repository.UserDataRepository
import ir.famastudio.ravan.core.data.repository.WordRepository
import ir.famastudio.ravan.core.domain.GetWordDetailsUseCase
import ir.famastudio.ravan.core.domain.WordDetails
import ir.famastudio.ravan.core.model.Accent
import ir.famastudio.ravan.core.model.UserData
import ir.famastudio.ravan.core.speech.Pronouncer
import ir.famastudio.ravan.feature.word.navigation.WordDetailRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

sealed interface WordDetailUiState {
    data object Loading : WordDetailUiState
    data object NotFound : WordDetailUiState
    data class Success(
        val details: WordDetails,
        val defaultAccent: Accent,
        val now: Long,
    ) : WordDetailUiState
}

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getWordDetails: GetWordDetailsUseCase,
    private val wordRepository: WordRepository,
    private val reviewRepository: ReviewRepository,
    private val deckRepository: DeckRepository,
    userDataRepository: UserDataRepository,
    private val pronouncer: Pronouncer,
    private val clock: Clock,
) : ViewModel() {

    private val wordId = savedStateHandle.toRoute<WordDetailRoute>().wordId

    private val userData = userDataRepository.userData
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserData())

    val uiState: StateFlow<WordDetailUiState> = combine(getWordDetails(wordId), userData) { details, user ->
        if (details == null) WordDetailUiState.NotFound
        else WordDetailUiState.Success(details, user.defaultAccent, clock.millis())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WordDetailUiState.Loading)

    val speakingText: StateFlow<String?> = pronouncer.speakingText

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    init {
        viewModelScope.launch { wordRepository.markViewed(wordId) }
    }

    fun speak(text: String, accent: Accent = userData.value.defaultAccent) =
        pronouncer.speak(text, accent, userData.value.speechRate)

    fun toggleFavorite() = withWord { details ->
        wordRepository.setFavorite(wordId, !details.word.isFavorite)
    }

    fun toggleReview() = withWord { details ->
        if (details.card == null) reviewRepository.addToReview(wordId) else reviewRepository.removeFromReview(wordId)
    }

    fun setDecks(deckIds: Set<String>) {
        viewModelScope.launch { deckRepository.setDecksForWord(wordId, deckIds) }
    }

    fun createDeckWithWord(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val deckId = deckRepository.createDeck(name, colorIndex = (0..7).random())
            deckRepository.addWordsToDeck(deckId, listOf(wordId))
        }
    }

    fun delete() {
        viewModelScope.launch {
            pronouncer.stop()
            wordRepository.deleteWord(wordId)
            _isDeleted.value = true
        }
    }

    private fun withWord(block: suspend (WordDetails) -> Unit) {
        val state = uiState.value as? WordDetailUiState.Success ?: return
        viewModelScope.launch { block(state.details) }
    }
}
