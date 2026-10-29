package ir.famastudio.ravan.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import ir.famastudio.ravan.core.database.model.WordEntity
import kotlinx.coroutines.flow.Flow

private const val WORD_OF_DAY_POOL =
    "is_user_added = 0 AND level IN ('B1', 'B2', 'C1') AND examples != '[]'"

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE is_favorite = 1 ORDER BY text_lower")
    fun observeFavorites(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE is_user_added = 1 ORDER BY created_at DESC")
    fun observeUserAdded(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id IN (SELECT word_id FROM review_cards) ORDER BY text_lower")
    fun observeInReview(): Flow<List<WordEntity>>

    /** Candidates for "word of the day": bundled, intermediate-level words that have examples. */
    @Query("SELECT COUNT(*) FROM words WHERE $WORD_OF_DAY_POOL")
    fun observeWordOfTheDayPoolSize(): Flow<Int>

    @Query("SELECT * FROM words WHERE $WORD_OF_DAY_POOL ORDER BY id LIMIT 1 OFFSET :offset")
    fun observeWordOfTheDayAt(offset: Int): Flow<WordEntity?>

    @Query("SELECT * FROM words WHERE last_viewed_at IS NOT NULL ORDER BY last_viewed_at DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM words WHERE id = :id")
    fun observeById(id: String): Flow<WordEntity?>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: String): WordEntity?

    @Query("SELECT * FROM words WHERE text_lower = :normalizedText LIMIT 1")
    suspend fun getByNormalizedText(normalizedText: String): WordEntity?

    @Query("SELECT * FROM words WHERE text_lower IN (:normalizedTexts)")
    suspend fun getByNormalizedTexts(normalizedTexts: List<String>): List<WordEntity>

    @Query(
        """
        SELECT * FROM words
        WHERE search_text LIKE '%' || :query || '%'
        ORDER BY
            CASE
                WHEN text_lower = :query THEN 0
                WHEN text_lower LIKE :query || '%' THEN 1
                WHEN search_text LIKE :query || '%' THEN 2
                ELSE 3
            END,
            CASE WHEN frequency_rank = 0 THEN 1000000 ELSE frequency_rank END,
            text_lower
        LIMIT :limit
        """,
    )
    fun search(query: String, limit: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words")
    suspend fun getAll(): List<WordEntity>

    @Upsert
    suspend fun upsert(word: WordEntity)

    @Upsert
    suspend fun upsertAll(words: List<WordEntity>)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun count(): Int

    /** Used by the seeder: never overwrites an entry the user may have edited. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoringExisting(words: List<WordEntity>)

    @Query("UPDATE words SET is_favorite = :favorite, updated_at = :now WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean, now: Long)

    @Query("UPDATE words SET last_viewed_at = :now WHERE id = :id")
    suspend fun markViewed(id: String, now: Long)

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun delete(id: String)
}
