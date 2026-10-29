package ir.famastudio.ravan.core.data.backup

import kotlinx.serialization.Serializable

/**
 * Cross-platform backup format (identical on the iOS app).
 * All timestamps are Unix epoch milliseconds.
 */
@Serializable
data class BackupDocument(
    val app: String = APP_ID,
    val formatVersion: Int = FORMAT_VERSION,
    val exportedAt: Long,
    val platform: String = "android",
    val words: List<BackupWord> = emptyList(),
    val cards: List<BackupCard> = emptyList(),
    val decks: List<BackupDeck> = emptyList(),
    val reviewLogs: List<BackupReviewLog> = emptyList(),
) {
    companion object {
        const val APP_ID = "ravan"
        const val FORMAT_VERSION = 1
    }
}

@Serializable
data class BackupWord(
    val id: String,
    val text: String,
    val phoneticUs: String = "",
    val phoneticUk: String = "",
    val partOfSpeech: String = "other",
    val level: String? = null,
    val meanings: List<BackupMeaning> = emptyList(),
    val examples: List<BackupExample> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val note: String = "",
    val isFavorite: Boolean = false,
    val isUserAdded: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val lastViewedAt: Long? = null,
    val frequencyRank: Int = 0,
)

@Serializable
data class BackupMeaning(val translation: String, val description: String = "")

@Serializable
data class BackupExample(val english: String, val persian: String = "")

@Serializable
data class BackupCard(
    val wordId: String,
    val box: Int,
    val dueAt: Long,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val lastReviewedAt: Long? = null,
    val addedAt: Long = 0,
)

@Serializable
data class BackupDeck(
    val id: String,
    val name: String,
    val colorIndex: Int = 0,
    val createdAt: Long = 0,
    val wordIds: List<String> = emptyList(),
)

@Serializable
data class BackupReviewLog(
    val id: String,
    val wordId: String,
    val reviewedAt: Long,
    val correct: Boolean,
)

class InvalidBackupException(message: String, cause: Throwable? = null) : Exception(message, cause)
