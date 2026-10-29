package ir.famastudio.ravan.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import ir.famastudio.ravan.core.database.model.DailyActivityRow
import ir.famastudio.ravan.core.database.model.PopulatedReviewCard
import ir.famastudio.ravan.core.database.model.ReviewCardEntity
import ir.famastudio.ravan.core.database.model.ReviewLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM review_cards WHERE word_id = :wordId")
    fun observeCard(wordId: String): Flow<ReviewCardEntity?>

    @Query("SELECT * FROM review_cards")
    fun observeCards(): Flow<List<ReviewCardEntity>>

    @Query("SELECT * FROM review_cards")
    suspend fun getCards(): List<ReviewCardEntity>

    /**
     * Cards for a session. `deckId = null` means every deck; `includeNotDue = 1` is free practice.
     * Lower boxes first: those are the words the user knows least.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM review_cards
        WHERE (:includeNotDue = 1 OR due_at <= :now)
          AND (:deckId IS NULL OR word_id IN (SELECT word_id FROM deck_words WHERE deck_id = :deckId))
        ORDER BY box, due_at
        LIMIT :limit
        """,
    )
    suspend fun getSessionCards(now: Long, deckId: String?, includeNotDue: Boolean, limit: Int): List<PopulatedReviewCard>

    @Upsert
    suspend fun upsertCard(card: ReviewCardEntity)

    @Upsert
    suspend fun upsertCards(cards: List<ReviewCardEntity>)

    @Query("DELETE FROM review_cards WHERE word_id = :wordId")
    suspend fun deleteCard(wordId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReviewLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ReviewLogEntity>)

    @Query("SELECT * FROM review_logs")
    suspend fun getLogs(): List<ReviewLogEntity>

    @Query(
        """
        SELECT epoch_day, COUNT(*) AS reviewed, SUM(CASE WHEN correct THEN 1 ELSE 0 END) AS correct
        FROM review_logs
        WHERE epoch_day >= :fromEpochDay
        GROUP BY epoch_day
        ORDER BY epoch_day
        """,
    )
    fun observeDailyActivity(fromEpochDay: Long): Flow<List<DailyActivityRow>>

    @Query("SELECT DISTINCT epoch_day FROM review_logs ORDER BY epoch_day DESC")
    fun observeActiveDays(): Flow<List<Long>>
}
