package ir.famastudio.ravan.feature.review.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.famastudio.ravan.core.data.repository.ReviewRepository
import ir.famastudio.ravan.core.data.repository.UserDataRepository
import ir.famastudio.ravan.core.domain.StartReviewSessionUseCase
import ir.famastudio.ravan.core.model.ReviewDirection
import ir.famastudio.ravan.core.model.ReviewItem
import ir.famastudio.ravan.core.model.UserData
import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.core.speech.Pronouncer
import ir.famastudio.ravan.feature.review.navigation.ReviewSessionRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReviewSessionUiState {
    data object Loading : ReviewSessionUiState
    data object Empty : ReviewSessionUiState
    data class InProgress(
        val items: List<ReviewItem>,
        val index: Int,
        val isFlipped: Boolean,
        val correctCount: Int,
        val isPractice: Boolean,
    ) : ReviewSessionUiState {
        val current: ReviewItem get() = items[index]
        val progress: Float get() = index / items.size.toFloat()
    }
    data class Finished(
        val total: Int,
        val correct: Int,
        val toRetry: List<Word>,
    ) : ReviewSessionUiState
}

@HiltViewModel
class ReviewSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startReviewSession: StartReviewSessionUseCase,
    private val reviewRepository: ReviewRepository,
    private val userDataRepository: UserDataRepository,
    private val pronouncer: Pronouncer,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ReviewSessionRoute>()
    private var userData = UserData()
    private val wrongWords = mutableListOf<Word>()

    private val _uiState = MutableStateFlow<ReviewSessionUiState>(ReviewSessionUiState.Loading)
    val uiState: StateFlow<ReviewSessionUiState> = _uiState.asStateFlow()

    val speakingText: StateFlow<String?> = pronouncer.speakingText

    init {
        viewModelScope.launch {
            userData = userDataRepository.userData.first()
            val items = startReviewSession(deckId = route.deckId, practice = route.practice)
            _uiState.value = if (items.isEmpty()) {
                ReviewSessionUiState.Empty
            } else {
                ReviewSessionUiState.InProgress(items, index = 0, isFlipped = false, correctCount = 0, isPractice = route.practice)
                    .also(::autoPronounceFront)
            }
        }
    }

    fun flip() {
        val state = _uiState.value as? ReviewSessionUiState.InProgress ?: return
        val flipped = state.copy(isFlipped = !state.isFlipped)
        _uiState.value = flipped
        // Persian → English cards reveal the English word on the back: say it then.
        if (flipped.isFlipped && flipped.current.direction == ReviewDirection.PERSIAN_TO_ENGLISH && userData.autoPronounce) {
            speak(flipped.current.word.text)
        }
    }

    fun answer(correct: Boolean) {
        val state = _uiState.value as? ReviewSessionUiState.InProgress ?: return
        val item = state.current
        if (!correct) wrongWords += item.word
        viewModelScope.launch {
            reviewRepository.recordAnswer(item.card, correct, reschedule = !state.isPractice)
        }
        val correctCount = state.correctCount + if (correct) 1 else 0
        val nextIndex = state.index + 1
        _uiState.value = if (nextIndex >= state.items.size) {
            pronouncer.stop()
            ReviewSessionUiState.Finished(total = state.items.size, correct = correctCount, toRetry = wrongWords.toList())
        } else {
            state.copy(index = nextIndex, isFlipped = false, correctCount = correctCount).also(::autoPronounceFront)
        }
    }

    fun speak(text: String) = pronouncer.speak(text, userData.defaultAccent, userData.speechRate)

    private fun autoPronounceFront(state: ReviewSessionUiState.InProgress) {
        if (userData.autoPronounce && state.current.direction == ReviewDirection.ENGLISH_TO_PERSIAN) {
            speak(state.current.word.text)
        }
    }

    override fun onCleared() {
        pronouncer.stop()
    }
}
