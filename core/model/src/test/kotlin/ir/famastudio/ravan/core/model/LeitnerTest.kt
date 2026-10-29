package ir.famastudio.ravan.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class LeitnerTest {
    private val now = 1_700_000_000_000L
    private val day = TimeUnit.DAYS.toMillis(1)

    @Test
    fun correctAnswer_movesCardForward() {
        val card = Leitner.newCard("w", now)
        val next = Leitner.answer(card, correct = true, now = now)
        assertEquals(2, next.box)
        assertEquals(now + 2 * day, next.dueAt)
        assertEquals(1, next.correctCount)
    }

    @Test
    fun wrongAnswer_resetsToFirstBox() {
        val card = Leitner.newCard("w", now).copy(box = 4)
        val next = Leitner.answer(card, correct = false, now = now)
        assertEquals(1, next.box)
        assertEquals(now, next.dueAt)
        assertEquals(0, next.correctCount)
        assertEquals(1, next.reviewCount)
    }

    @Test
    fun lastBox_becomesMastered() {
        val card = Leitner.newCard("w", now).copy(box = Leitner.BOX_COUNT)
        val next = Leitner.answer(card, correct = true, now = now)
        assertTrue(next.isMastered)
        assertEquals(Leitner.MASTERED_BOX, Leitner.answer(next, true, now).box)
    }
}
