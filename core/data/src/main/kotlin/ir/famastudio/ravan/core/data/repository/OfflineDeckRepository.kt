package ir.famastudio.ravan.core.data.repository

import ir.famastudio.ravan.core.data.model.asExternalModel
import ir.famastudio.ravan.core.database.TransactionRunner
import ir.famastudio.ravan.core.database.dao.DeckDao
import ir.famastudio.ravan.core.database.model.DeckEntity
import ir.famastudio.ravan.core.database.model.DeckWithCount
import ir.famastudio.ravan.core.database.model.DeckWordCrossRef
import ir.famastudio.ravan.core.model.Deck
import ir.famastudio.ravan.core.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

internal class OfflineDeckRepository @Inject constructor(
    private val deckDao: DeckDao,
    private val transaction: TransactionRunner,
    private val clock: Clock,
) : DeckRepository {

    override fun observeDecks(): Flow<List<Deck>> =
        deckDao.observeDecks().map { decks -> decks.map { it.asExternalModel() } }

    override fun observeDeck(deckId: String): Flow<Deck?> =
        deckDao.observeDeck(deckId).map { it?.asExternalModel() }

    override fun observeDeckWords(deckId: String): Flow<List<Word>> =
        deckDao.observeDeckWords(deckId).map { words -> words.map { it.asExternalModel() } }

    override fun observeDeckIdsForWord(wordId: String): Flow<Set<String>> =
        deckDao.observeDeckIdsForWord(wordId).map { it.toSet() }

    override suspend fun createDeck(name: String, colorIndex: Int): String {
        val id = UUID.randomUUID().toString()
        deckDao.upsertDeck(DeckEntity(id = id, name = name.trim(), colorIndex = colorIndex, createdAt = clock.millis()))
        return id
    }

    override suspend fun updateDeck(deckId: String, name: String, colorIndex: Int) =
        deckDao.updateDeck(deckId, name.trim(), colorIndex)

    override suspend fun deleteDeck(deckId: String) = deckDao.deleteDeck(deckId)

    override suspend fun addWordsToDeck(deckId: String, wordIds: Collection<String>) =
        deckDao.insertCrossRefs(wordIds.map { DeckWordCrossRef(deckId = deckId, wordId = it) })

    override suspend fun removeWordFromDeck(deckId: String, wordId: String) =
        deckDao.deleteCrossRef(deckId, wordId)

    override suspend fun setDecksForWord(wordId: String, deckIds: Set<String>) = transaction.run {
        deckDao.deleteCrossRefsForWord(wordId)
        deckDao.insertCrossRefs(deckIds.map { DeckWordCrossRef(deckId = it, wordId = wordId) })
    }

    private fun DeckWithCount.asExternalModel() = Deck(
        id = deck.id,
        name = deck.name,
        colorIndex = deck.colorIndex,
        createdAt = deck.createdAt,
        wordCount = wordCount,
    )
}
