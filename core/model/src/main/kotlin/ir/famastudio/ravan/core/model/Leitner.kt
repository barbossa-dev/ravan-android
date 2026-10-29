package ir.famastudio.ravan.core.model

import java.util.concurrent.TimeUnit

/**
 * Classic Leitner spaced-repetition:
 *  - a correct answer moves the card one box forward and pushes it further into the future;
 *  - a wrong answer sends it back to box 1, due again immediately.
 * Kept pure so it is trivially unit-testable and identical on iOS.
 */
object Leitner {
    const val BOX_COUNT = 5
    const val MASTERED_BOX = BOX_COUNT + 1

    /** Days to wait after landing in box n (index = box - 1). Last entry is for mastered cards. */
    private val intervalDays = longArrayOf(1, 2, 4, 8, 16, 60)

    fun newCard(wordId: String, now: Long) = ReviewCard(
        wordId = wordId,
        box = 1,
        dueAt = now,
        addedAt = now,
    )

    fun answer(card: ReviewCard, correct: Boolean, now: Long): ReviewCard {
        val nextBox = if (correct) (card.box + 1).coerceAtMost(MASTERED_BOX) else 1
        val dueAt = if (correct) now + TimeUnit.DAYS.toMillis(intervalDays[nextBox - 1]) else now
        return card.copy(
            box = nextBox,
            dueAt = dueAt,
            reviewCount = card.reviewCount + 1,
            correctCount = card.correctCount + if (correct) 1 else 0,
            lastReviewedAt = now,
        )
    }

    fun intervalForBox(box: Int): Long = intervalDays[(box - 1).coerceIn(0, intervalDays.lastIndex)]
}
