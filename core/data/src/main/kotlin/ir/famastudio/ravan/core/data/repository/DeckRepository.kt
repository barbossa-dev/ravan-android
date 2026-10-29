package ir.famastudio.ravan.core.data.repository

import ir.famastudio.ravan.core.model.Deck
import ir.famastudio.ravan.core.model.Word
import kotlinx.coroutines.flow.Flow

interface DeckRepository {
    fun observeDecks(): Flow<List<Deck>>
    fun observeDeck(deckId: String): Flow<Deck?>
    fun observeDeckWords(deckId: String): Flow<List<Word>>
    fun observeDeckIdsForWord(wordId: String): Flow<Set<String>>

    suspend fun createDeck(name: String, colorIndex: Int): String
    suspend fun updateDeck(deckId: String, name: String, colorIndex: Int)
    suspend fun deleteDeck(deckId: String)
    suspend fun addWordsToDeck(deckId: String, wordIds: Collection<String>)
    suspend fun removeWordFromDeck(deckId: String, wordId: String)
    suspend fun setDecksForWord(wordId: String, deckIds: Set<String>)
}
