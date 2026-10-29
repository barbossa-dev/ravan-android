package ir.famastudio.ravan.feature.editor

import ir.famastudio.ravan.core.model.CefrLevel
import ir.famastudio.ravan.core.model.PartOfSpeech

data class MeaningInput(val translation: String = "", val description: String = "")

data class ExampleInput(val english: String = "", val persian: String = "")

enum class EditorError { EMPTY_WORD, NO_MEANING, DUPLICATE }

data class WordEditorUiState(
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    val text: String = "",
    val phoneticUs: String = "",
    val phoneticUk: String = "",
    val partOfSpeech: PartOfSpeech = PartOfSpeech.NOUN,
    val level: CefrLevel? = null,
    val meanings: List<MeaningInput> = listOf(MeaningInput()),
    val examples: List<ExampleInput> = listOf(ExampleInput()),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val note: String = "",
    val addToReview: Boolean = true,
    val isSaving: Boolean = false,
    val error: EditorError? = null,
    val duplicateWordId: String? = null,
    val savedWordId: String? = null,
)
