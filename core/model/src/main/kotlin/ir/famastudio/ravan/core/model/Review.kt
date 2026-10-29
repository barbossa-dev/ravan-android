package ir.famastudio.ravan.core.model

/** Leitner state of one word. `box` runs 1..[Leitner.BOX_COUNT]; [Leitner.MASTERED_BOX] means learned. */
data class ReviewCard(
    val wordId: String,
    val box: Int = 1,
    val dueAt: Long,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val lastReviewedAt: Long? = null,
    val addedAt: Long,
) {
    val isMastered: Boolean get() = box >= Leitner.MASTERED_BOX
    fun isDue(now: Long): Boolean = dueAt <= now
}

data class ReviewLog(
    val id: String,
    val wordId: String,
    val reviewedAt: Long,
    val correct: Boolean,
)

/** One card shown in a review session. */
data class ReviewItem(
    val word: Word,
    val card: ReviewCard,
    val direction: ReviewDirection,
)

enum class ReviewDirection { ENGLISH_TO_PERSIAN, PERSIAN_TO_ENGLISH, MIXED }

data class DayActivity(
    val epochDay: Long,
    val reviewed: Int,
    val correct: Int,
)

data class ReviewStats(
    val dueCount: Int,
    val totalCards: Int,
    val masteredCount: Int,
    /** Index 0..4 → boxes 1..5, index 5 → mastered. */
    val boxCounts: List<Int>,
    val streakDays: Int,
    val reviewedToday: Int,
    val dailyGoal: Int,
    val lastSevenDays: List<DayActivity>,
) {
    val accuracy: Float
        get() {
            val reviewed = lastSevenDays.sumOf { it.reviewed }
            return if (reviewed == 0) 0f else lastSevenDays.sumOf { it.correct } / reviewed.toFloat()
        }
}

data class WordWithCard(
    val word: Word,
    val card: ReviewCard,
)
