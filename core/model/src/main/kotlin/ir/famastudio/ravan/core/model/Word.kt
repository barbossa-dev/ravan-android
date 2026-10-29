package ir.famastudio.ravan.core.model

/**
 * A dictionary entry. Seeded entries and words added by the user share the same shape,
 * which keeps search, review and backup completely uniform.
 */
data class Word(
    val id: String,
    val text: String,
    val phoneticUs: String = "",
    val phoneticUk: String = "",
    val partOfSpeech: PartOfSpeech = PartOfSpeech.OTHER,
    val level: CefrLevel? = null,
    val meanings: List<Meaning> = emptyList(),
    val examples: List<Example> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val note: String = "",
    val isFavorite: Boolean = false,
    val isUserAdded: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val lastViewedAt: Long? = null,
    /** Corpus frequency rank (1 = most common); 0 when unknown, e.g. for words the user added. */
    val frequencyRank: Int = 0,
) {
    /** The first translation, used in compact list rows. */
    val primaryTranslation: String
        get() = meanings.firstOrNull()?.translation.orEmpty()

    fun phonetic(accent: Accent): String = when (accent) {
        Accent.US -> phoneticUs.ifBlank { phoneticUk }
        Accent.UK -> phoneticUk.ifBlank { phoneticUs }
    }
}

data class Meaning(
    val translation: String,
    val description: String = "",
)

data class Example(
    val english: String,
    val persian: String = "",
)

enum class PartOfSpeech(val key: String, val persianLabel: String) {
    NOUN("noun", "اسم"),
    VERB("verb", "فعل"),
    ADJECTIVE("adjective", "صفت"),
    ADVERB("adverb", "قید"),
    PRONOUN("pronoun", "ضمیر"),
    PREPOSITION("preposition", "حرف اضافه"),
    CONJUNCTION("conjunction", "حرف ربط"),
    DETERMINER("determiner", "صفت اشاره/شمارشی"),
    INTERJECTION("interjection", "صوت"),
    PHRASE("phrase", "عبارت"),
    OTHER("other", "سایر"),
    ;

    companion object {
        fun fromKey(key: String?): PartOfSpeech =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: OTHER
    }
}

enum class CefrLevel {
    A1, A2, B1, B2, C1, C2;

    companion object {
        fun fromKey(key: String?): CefrLevel? =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) }
    }
}

enum class Accent { US, UK }
