package ir.famastudio.ravan.core.data.repository

import ir.famastudio.ravan.core.model.DayActivity
import ir.famastudio.ravan.core.model.ReviewCard
import ir.famastudio.ravan.core.model.WordWithCard
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeCard(wordId: String): Flow<ReviewCard?>
    fun observeCards(): Flow<List<ReviewCard>>
    fun observeDailyActivity(fromEpochDay: Long): Flow<List<DayActivity>>
    fun observeActiveDays(): Flow<List<Long>>

    suspend fun addToReview(wordId: String)
    suspend fun addAllToReview(wordIds: Collection<String>)
    suspend fun removeFromReview(wordId: String)

    suspend fun getSessionCards(deckId: String?, includeNotDue: Boolean, limit: Int): List<WordWithCard>

    /**
     * Logs an answer. When [reschedule] is false (free practice) the Leitner box is left untouched.
     */
    suspend fun recordAnswer(card: ReviewCard, correct: Boolean, reschedule: Boolean): ReviewCard
}
