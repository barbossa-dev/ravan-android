package ir.famastudio.ravan.core.domain

import ir.famastudio.ravan.core.data.repository.ReviewRepository
import ir.famastudio.ravan.core.data.repository.UserDataRepository
import ir.famastudio.ravan.core.model.ReviewDirection
import ir.famastudio.ravan.core.model.ReviewItem
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

class StartReviewSessionUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val userDataRepository: UserDataRepository,
) {
    /**
     * @param deckId restrict to one deck, or null for everything.
     * @param practice free practice: also include cards that are not due yet.
     */
    suspend operator fun invoke(deckId: String?, practice: Boolean, random: Random = Random.Default): List<ReviewItem> {
        val userData = userDataRepository.userData.first()
        val limit = maxOf(userData.dailyGoal, MIN_SESSION_SIZE)
        return reviewRepository.getSessionCards(deckId = deckId, includeNotDue = practice, limit = limit)
            .shuffled(random)
            .map { (word, card) ->
                val direction = when (userData.reviewDirection) {
                    ReviewDirection.MIXED ->
                        if (random.nextBoolean()) ReviewDirection.ENGLISH_TO_PERSIAN else ReviewDirection.PERSIAN_TO_ENGLISH
                    else -> userData.reviewDirection
                }
                ReviewItem(word = word, card = card, direction = direction)
            }
    }

    private companion object {
        const val MIN_SESSION_SIZE = 10
    }
}
