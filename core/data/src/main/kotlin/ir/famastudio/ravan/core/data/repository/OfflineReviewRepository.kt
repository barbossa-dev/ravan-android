package ir.famastudio.ravan.core.data.repository

import ir.famastudio.ravan.core.data.model.asEntity
import ir.famastudio.ravan.core.data.model.asExternalModel
import ir.famastudio.ravan.core.database.TransactionRunner
import ir.famastudio.ravan.core.database.dao.ReviewDao
import ir.famastudio.ravan.core.database.model.ReviewLogEntity
import ir.famastudio.ravan.core.model.DayActivity
import ir.famastudio.ravan.core.model.Leitner
import ir.famastudio.ravan.core.model.ReviewCard
import ir.famastudio.ravan.core.model.WordWithCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

internal class OfflineReviewRepository @Inject constructor(
    private val reviewDao: ReviewDao,
    private val transaction: TransactionRunner,
    private val clock: Clock,
) : ReviewRepository {

    override fun observeCard(wordId: String): Flow<ReviewCard?> =
        reviewDao.observeCard(wordId).map { it?.asExternalModel() }

    override fun observeCards(): Flow<List<ReviewCard>> =
        reviewDao.observeCards().map { cards -> cards.map { it.asExternalModel() } }

    override fun observeDailyActivity(fromEpochDay: Long): Flow<List<DayActivity>> =
        reviewDao.observeDailyActivity(fromEpochDay).map { rows ->
            rows.map { DayActivity(it.epochDay, it.reviewed, it.correct) }
        }

    override fun observeActiveDays(): Flow<List<Long>> = reviewDao.observeActiveDays()

    override suspend fun addToReview(wordId: String) = addAllToReview(listOf(wordId))

    override suspend fun addAllToReview(wordIds: Collection<String>) {
        val now = clock.millis()
        val existing = reviewDao.getCards().mapTo(HashSet()) { it.wordId }
        val newCards = wordIds.filterNot(existing::contains).map { Leitner.newCard(it, now).asEntity() }
        if (newCards.isNotEmpty()) reviewDao.upsertCards(newCards)
    }

    override suspend fun removeFromReview(wordId: String) = reviewDao.deleteCard(wordId)

    override suspend fun getSessionCards(deckId: String?, includeNotDue: Boolean, limit: Int): List<WordWithCard> =
        reviewDao.getSessionCards(clock.millis(), deckId, includeNotDue, limit).map {
            WordWithCard(word = it.word.asExternalModel(), card = it.card.asExternalModel())
        }

    override suspend fun recordAnswer(card: ReviewCard, correct: Boolean, reschedule: Boolean): ReviewCard {
        val now = clock.millis()
        val updated = if (reschedule) {
            Leitner.answer(card, correct, now)
        } else {
            card.copy(
                reviewCount = card.reviewCount + 1,
                correctCount = card.correctCount + if (correct) 1 else 0,
                lastReviewedAt = now,
            )
        }
        transaction.run {
            reviewDao.upsertCard(updated.asEntity())
            reviewDao.insertLog(
                ReviewLogEntity(
                    id = UUID.randomUUID().toString(),
                    wordId = card.wordId,
                    reviewedAt = now,
                    epochDay = epochDayOf(now),
                    correct = correct,
                ),
            )
        }
        return updated
    }

    private fun epochDayOf(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(clock.zone).toLocalDate().toEpochDay()
}
