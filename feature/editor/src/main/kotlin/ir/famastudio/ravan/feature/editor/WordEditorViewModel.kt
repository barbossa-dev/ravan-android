package ir.famastudio.ravan.feature.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.famastudio.ravan.core.data.repository.ReviewRepository
import ir.famastudio.ravan.core.data.repository.WordRepository
import ir.famastudio.ravan.core.model.CefrLevel
import ir.famastudio.ravan.core.model.Example
import ir.famastudio.ravan.core.model.Meaning
import ir.famastudio.ravan.core.model.PartOfSpeech
import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.feature.editor.navigation.WordEditorRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<WordEditorRoute>()
    private var original: Word? = null

    private val _uiState = MutableStateFlow(
        WordEditorUiState(isNew = route.wordId == null, text = route.initialText.orEmpty(), isLoading = route.wordId != null),
    )
    val uiState: StateFlow<WordEditorUiState> = _uiState.asStateFlow()

    init {
        route.wordId?.let { id ->
            viewModelScope.launch {
                val word = wordRepository.getWord(id)
                original = word
                _uiState.value = word?.toUiState() ?: WordEditorUiState()
            }
        }
    }

    fun onTextChange(value: String) = _uiState.update { it.copy(text = value, error = null, duplicateWordId = null) }
    fun onPhoneticUsChange(value: String) = _uiState.update { it.copy(phoneticUs = value) }
    fun onPhoneticUkChange(value: String) = _uiState.update { it.copy(phoneticUk = value) }
    fun onPartOfSpeechChange(value: PartOfSpeech) = _uiState.update { it.copy(partOfSpeech = value) }
    fun onLevelChange(value: CefrLevel?) = _uiState.update { it.copy(level = value) }
    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }
    fun onAddToReviewChange(value: Boolean) = _uiState.update { it.copy(addToReview = value) }

    fun onMeaningChange(index: Int, value: MeaningInput) = _uiState.update {
        it.copy(meanings = it.meanings.replaceAt(index, value), error = null)
    }
    fun addMeaning() = _uiState.update { it.copy(meanings = it.meanings + MeaningInput()) }
    fun removeMeaning(index: Int) = _uiState.update { it.copy(meanings = it.meanings.removeAtOrKeepOne(index) { MeaningInput() }) }

    fun onExampleChange(index: Int, value: ExampleInput) = _uiState.update { it.copy(examples = it.examples.replaceAt(index, value)) }
    fun addExample() = _uiState.update { it.copy(examples = it.examples + ExampleInput()) }
    fun removeExample(index: Int) = _uiState.update { it.copy(examples = it.examples.removeAtOrKeepOne(index) { ExampleInput() }) }

    fun addSynonym(value: String) = _uiState.update { it.copy(synonyms = it.synonyms.plusUnique(value)) }
    fun removeSynonym(value: String) = _uiState.update { it.copy(synonyms = it.synonyms - value) }
    fun addAntonym(value: String) = _uiState.update { it.copy(antonyms = it.antonyms.plusUnique(value)) }
    fun removeAntonym(value: String) = _uiState.update { it.copy(antonyms = it.antonyms - value) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        val error = when {
            state.text.isBlank() -> EditorError.EMPTY_WORD
            state.meanings.none { it.translation.isNotBlank() } -> EditorError.NO_MEANING
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val existing = wordRepository.findByText(state.text)
            if (existing != null && existing.id != original?.id) {
                _uiState.update { it.copy(isSaving = false, error = EditorError.DUPLICATE, duplicateWordId = existing.id) }
                return@launch
            }
            val base = original ?: Word(id = "", text = "")
            val word = base.copy(
                text = state.text.trim(),
                phoneticUs = state.phoneticUs.trim(),
                phoneticUk = state.phoneticUk.trim(),
                partOfSpeech = state.partOfSpeech,
                level = state.level,
                meanings = state.meanings
                    .filter { it.translation.isNotBlank() }
                    .map { Meaning(it.translation.trim(), it.description.trim()) },
                examples = state.examples
                    .filter { it.english.isNotBlank() }
                    .map { Example(it.english.trim(), it.persian.trim()) },
                synonyms = state.synonyms,
                antonyms = state.antonyms,
                note = state.note.trim(),
            )
            val id = wordRepository.saveWord(word)
            if (state.isNew && state.addToReview) reviewRepository.addToReview(id)
            _uiState.update { it.copy(isSaving = false, savedWordId = id) }
        }
    }

    private fun Word.toUiState() = WordEditorUiState(
        isNew = false,
        text = text,
        phoneticUs = phoneticUs,
        phoneticUk = phoneticUk,
        partOfSpeech = partOfSpeech,
        level = level,
        meanings = meanings.map { MeaningInput(it.translation, it.description) }.ifEmpty { listOf(MeaningInput()) },
        examples = examples.map { ExampleInput(it.english, it.persian) }.ifEmpty { listOf(ExampleInput()) },
        synonyms = synonyms,
        antonyms = antonyms,
        note = note,
        addToReview = false,
    )
}

private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
    mapIndexed { i, item -> if (i == index) value else item }

private fun <T> List<T>.removeAtOrKeepOne(index: Int, empty: () -> T): List<T> =
    filterIndexed { i, _ -> i != index }.ifEmpty { listOf(empty()) }

internal fun List<String>.plusUnique(value: String): List<String> {
    val trimmed = value.trim()
    return if (trimmed.isEmpty() || any { it.equals(trimmed, ignoreCase = true) }) this else this + trimmed
}
