package ir.famastudio.ravan.core.common.text

private const val PERSIAN_DIGITS = "۰۱۲۳۴۵۶۷۸۹"

/** "12" → "۱۲". */
fun String.toPersianDigits(): String = buildString(length) {
    for (c in this@toPersianDigits) append(if (c in '0'..'9') PERSIAN_DIGITS[c - '0'] else c)
}

fun Int.toPersianDigits(): String = toString().toPersianDigits()
fun Long.toPersianDigits(): String = toString().toPersianDigits()

/**
 * Normalises text for searching so that Arabic/Persian letter variants, ZWNJ and case
 * differences do not prevent a match ("ك" vs "ک", "مي‌شود" vs "میشود", "Water" vs "water").
 */
object SearchNormalizer {
    fun normalize(input: String): String = buildString(input.length) {
        for (c in input.trim().lowercase()) {
            when (c) {
                'ي', 'ى' -> append('ی')
                'ك' -> append('ک')
                'ة' -> append('ه')
                'أ', 'إ', 'آ' -> append('ا')
                '‌', '‏', '‎', 'ـ' -> Unit
                in 'ً'..'ْ' -> Unit // Arabic diacritics (harakat)
                in '۰'..'۹' -> append('0' + (c - '۰'))
                else -> append(c)
            }
        }
    }
}
