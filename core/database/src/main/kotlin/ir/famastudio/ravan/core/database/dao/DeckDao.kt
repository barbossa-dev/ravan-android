package ir.famastudio.ravan.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import ir.famastudio.ravan.core.database.model.DeckEntity
import ir.famastudio.ravan.core.database.model.DeckWithCount
import ir.famastudio.ravan.core.database.model.DeckWordCrossRef
import ir.famastudio.ravan.core.database.model.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query(
        """
        SELECT decks.*, (SELECT COUNT(*) FROM deck_words WHERE deck_words.deck_id = decks.id) AS word_count
        FROM decks
        ORDER BY created_at DESC
        """,
    )
    fun observeDecks(): Flow<List<DeckWithCount>>

    @Query(
        """
        SELECT decks.*, (SELECT COUNT(*) FROM deck_words WHERE deck_words.deck_id = decks.id) AS word_count
        FROM decks WHERE id = :deckId
        """,
    )
    fun observeDeck(deckId: String): Flow<DeckWithCount?>

    @Query(
        """
        SELECT words.* FROM words
        INNER JOIN deck_words ON deck_words.word_id = words.id
        WHERE deck_words.deck_id = :deckId
        ORDER BY words.text_lower
        """,
    )
    fun observeDeckWords(deckId: String): Flow<List<WordEntity>>

    @Query("SELECT deck_id FROM deck_words WHERE word_id = :wordId")
    fun observeDeckIdsForWord(wordId: String): Flow<List<String>>

    @Query("SELECT * FROM decks")
    suspend fun getDecks(): List<DeckEntity>

    @Query("SELECT * FROM deck_words")
    suspend fun getCrossRefs(): List<DeckWordCrossRef>

    @Upsert
    suspend fun upsertDeck(deck: DeckEntity)

    @Upsert
    suspend fun upsertDecks(decks: List<DeckEntity>)

    @Query("UPDATE decks SET name = :name, color_index = :colorIndex WHERE id = :deckId")
    suspend fun updateDeck(deckId: String, name: String, colorIndex: Int)

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeck(deckId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(refs: List<DeckWordCrossRef>)

    @Query("DELETE FROM deck_words WHERE deck_id = :deckId AND word_id = :wordId")
    suspend fun deleteCrossRef(deckId: String, wordId: String)

    @Query("DELETE FROM deck_words WHERE word_id = :wordId")
    suspend fun deleteCrossRefsForWord(wordId: String)
}
