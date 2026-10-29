package ir.famastudio.ravan.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.famastudio.ravan.core.data.repository.DeckRepository
import ir.famastudio.ravan.core.data.repository.WordRepository
import ir.famastudio.ravan.core.model.Deck
import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.core.model.WordFilter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab { WORDS, DECKS }

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    wordRepository: WordRepository,
    private val deckRepository: DeckRepository,
) : ViewModel() {

    val selectedTab: StateFlow<LibraryTab> = savedStateHandle.getStateFlow(TAB_KEY, LibraryTab.WORDS)
    val filter: StateFlow<WordFilter> = savedStateHandle.getStateFlow(FILTER_KEY, WordFilter.RECENT)

    /** null while loading. */
    val words: StateFlow<List<Word>?> = filter
        .flatMapLatest { wordRepository.observeWords(it) }
        .map<List<Word>, List<Word>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val decks: StateFlow<List<Deck>?> = deckRepository.observeDecks()
        .map<List<Deck>, List<Deck>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectTab(tab: LibraryTab) {
        savedStateHandle[TAB_KEY] = tab
    }

    fun setFilter(filter: WordFilter) {
        savedStateHandle[FILTER_KEY] = filter
    }

    fun createDeck(name: String, colorIndex: Int) {
        viewModelScope.launch { deckRepository.createDeck(name, colorIndex) }
    }

    private companion object {
        const val TAB_KEY = "tab"
        const val FILTER_KEY = "filter"
    }
}
