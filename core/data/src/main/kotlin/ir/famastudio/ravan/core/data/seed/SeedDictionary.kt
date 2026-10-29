package ir.famastudio.ravan.core.data.seed

import ir.famastudio.ravan.core.data.backup.BackupExample
import ir.famastudio.ravan.core.data.backup.BackupMeaning
import kotlinx.serialization.Serializable

/** Shape of assets/dictionary.json — the same file ships inside the iOS app. */
@Serializable
internal data class SeedDictionary(
    val version: Int,
    val attribution: List<String> = emptyList(),
    val words: List<SeedWord>,
)

@Serializable
internal data class SeedWord(
    val text: String,
    val phoneticUs: String = "",
    val phoneticUk: String = "",
    val partOfSpeech: String = "other",
    val level: String? = null,
    val meanings: List<BackupMeaning> = emptyList(),
    val examples: List<BackupExample> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val rank: Int = 0,
)
