package ir.famastudio.ravan.core.common

import ir.famastudio.ravan.core.common.text.SearchNormalizer
import ir.famastudio.ravan.core.common.text.toPersianDigits
import org.junit.Assert.assertEquals
import org.junit.Test

class PersianTextTest {
    @Test
    fun digits_areConverted() {
        assertEquals("۱۲ کارت", "12 کارت".toPersianDigits())
    }

    @Test
    fun normalizer_unifiesArabicLetters_andZwnj() {
        assertEquals(SearchNormalizer.normalize("میشود"), SearchNormalizer.normalize("مي‌شود"))
        assertEquals("water", SearchNormalizer.normalize("  Water "))
        assertEquals(SearchNormalizer.normalize("کتاب"), SearchNormalizer.normalize("كتاب"))
    }
}
