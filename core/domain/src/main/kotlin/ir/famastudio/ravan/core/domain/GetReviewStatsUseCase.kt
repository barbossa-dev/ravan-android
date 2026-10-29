package ir.famastudio.ravan.core.domain

import ir.famastudio.ravan.core.data.repository.ReviewRepository
import ir.famastudio.ravan.core.data.repository.UserDataRepository
import ir.famastudio.ravan.core.model.DayActivity
import ir.famastudio.ravan.core.model.Leitner
import ir.famastudio.ravan.core.model.ReviewStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class GetReviewStatsUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val userDataRepository: UserDataRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<ReviewStats> {
        val today = LocalDate.now(clock).toEpochDay()
        return combine(
            reviewRepository.observeCards(),
            reviewRepository.observeDailyActivity(fromEpochDay = today - 6),
            reviewRepository.observeActiveDays(),
            userDataRepository.userData,
        ) { cards, activity, activeDays, userData ->
            val now = clock.millis()
            val byDay = activity.associateBy { it.epochDay }
            ReviewStats(
                dueCount = cards.count { it.isDue(now) },
                totalCards = cards.size,
                masteredCount = cards.count { it.isMastered },
                boxCounts = (1..Leitner.MASTERED_BOX).map { box -> cards.count { it.box == box } },
                streakDays = calculateStreak(activeDays, today),
                reviewedToday = byDay[today]?.reviewed ?: 0,
                dailyGoal = userData.dailyGoal,
                lastSevenDays = (today - 6..today).map { day -> byDay[day] ?: DayActivity(day, 0, 0) },
            )
        }
    }
}

/**
 * Consecutive days with at least one review, ending today — or yesterday, so the streak
 * is not shown as broken before the user had a chance to review today.
 */
internal fun calculateStreak(activeDaysDescending: List<Long>, today: Long): Int {
    val days = activeDaysDescending.toHashSet()
    var cursor = when {
        today in days -> today
        today - 1 in days -> today - 1
        else -> return 0
    }
    var streak = 0
    while (cursor in days) {
        streak++
        cursor--
    }
    return streak
}
