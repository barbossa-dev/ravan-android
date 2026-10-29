package ir.famastudio.ravan.core.domain

import ir.famastudio.ravan.core.model.Deck
import ir.famastudio.ravan.core.model.ReviewCard
import ir.famastudio.ravan.core.model.Word

/** Everything the word screen needs, assembled from several repositories. */
data class WordDetails(
    val word: Word,
    val card: ReviewCard?,
    val decks: List<Deck>,
    val selectedDeckIds: Set<String>,
    /** Synonyms/antonyms that exist in the dictionary → their word id (makes them tappable). */
    val relatedWordIds: Map<String, String>,
)
