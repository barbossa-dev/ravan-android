package ir.famastudio.ravan.core.domain

import ir.famastudio.ravan.core.data.repository.DeckRepository
import ir.famastudio.ravan.core.data.repository.ReviewRepository
import ir.famastudio.ravan.core.data.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetWordDetailsUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val reviewRepository: ReviewRepository,
    private val deckRepository: DeckRepository,
) {
    operator fun invoke(wordId: String): Flow<WordDetails?> =
        wordRepository.observeWord(wordId).flatMapLatest { word ->
            if (word == null) return@flatMapLatest flowOf(null)
            val related = flow { emit(wordRepository.findIdsByTexts(word.synonyms + word.antonyms)) }
            combine(
                reviewRepository.observeCard(wordId),
                deckRepository.observeDecks(),
                deckRepository.observeDeckIdsForWord(wordId),
                related,
            ) { card, decks, selectedDeckIds, relatedIds ->
                WordDetails(
                    word = word,
                    card = card,
                    decks = decks,
                    selectedDeckIds = selectedDeckIds,
                    relatedWordIds = relatedIds,
                )
            }
        }
}
