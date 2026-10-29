package ir.famastudio.ravan.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class PlusUniqueTest {
    @Test
    fun ignoresBlankAndCaseInsensitiveDuplicates() {
        val list = listOf("tough")
        assertEquals(list, list.plusUnique("  "))
        assertEquals(list, list.plusUnique("Tough"))
        assertEquals(listOf("tough", "hardy"), list.plusUnique(" hardy "))
    }
}
