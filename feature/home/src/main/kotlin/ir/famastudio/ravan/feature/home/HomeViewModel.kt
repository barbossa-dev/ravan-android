package ir.famastudio.ravan.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.famastudio.ravan.core.data.repository.ReviewRepository
import ir.famastudio.ravan.core.data.repository.UserDataRepository
import ir.famastudio.ravan.core.data.repository.WordRepository
import ir.famastudio.ravan.core.model.UserData
import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.core.speech.Pronouncer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    reviewRepository: ReviewRepository,
    userDataRepository: UserDataRepository,
    private val pronouncer: Pronouncer,
    private val clock: Clock,
) : ViewModel() {

    val query: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_KEY, "")

    val speakingText: StateFlow<String?> = pronouncer.speakingText

    private val userData: StateFlow<UserData> = userDataRepository.userData
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserData())

    val searchState: StateFlow<SearchUiState> = query
        .debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS }
        .map(String::trim)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            if (q.isEmpty()) flowOf(SearchUiState.Idle)
            else wordRepository.search(q).map { SearchUiState.Results(q, it) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState.Idle)

    val feedState: StateFlow<HomeFeedUiState> = combine(
        wordRepository.observeWordOfTheDay(LocalDate.now(clock).toEpochDay()),
        wordRepository.observeRecentWords(),
        reviewRepository.observeCards(),
        wordRepository.observeWordCount(),
        userData,
    ) { wordOfTheDay, recent, cards, wordCount, user ->
        val now = clock.millis()
        HomeFeedUiState.Success(
            wordOfTheDay = wordOfTheDay,
            recentWords = recent,
            dueCount = cards.count { it.isDue(now) },
            wordCount = wordCount,
            accent = user.defaultAccent,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeFeedUiState.Loading)

    fun onQueryChange(query: String) {
        savedStateHandle[QUERY_KEY] = query
    }

    fun speak(word: Word) {
        val user = userData.value
        pronouncer.speak(word.text, user.defaultAccent, user.speechRate)
    }

    private companion object {
        const val QUERY_KEY = "query"
        const val SEARCH_DEBOUNCE_MS = 180L
    }
}
