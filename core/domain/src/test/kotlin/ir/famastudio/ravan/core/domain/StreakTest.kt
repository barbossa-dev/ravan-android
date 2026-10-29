package ir.famastudio.ravan.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakTest {
    @Test
    fun countsConsecutiveDaysEndingToday() {
        assertEquals(3, calculateStreak(listOf(100, 99, 98, 96), today = 100))
    }

    @Test
    fun streakSurvivesUntilEndOfToday() {
        assertEquals(2, calculateStreak(listOf(99, 98), today = 100))
    }

    @Test
    fun brokenStreakIsZero() {
        assertEquals(0, calculateStreak(listOf(97, 96), today = 100))
    }
}
