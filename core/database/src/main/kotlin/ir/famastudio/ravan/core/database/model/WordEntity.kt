package ir.famastudio.ravan.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "words",
    indices = [
        Index(value = ["text_lower"]),
        Index(value = ["is_favorite"]),
        Index(value = ["last_viewed_at"]),
        Index(value = ["level"]),
    ],
)
data class WordEntity(
    @PrimaryKey val id: String,
    val text: String,
    /** Normalised headword, used for exact/prefix ranking and duplicate detection. */
    @ColumnInfo(name = "text_lower") val textLower: String,
    /** Normalised headword + translations + synonyms; the column the search runs on. */
    @ColumnInfo(name = "search_text") val searchText: String,
    @ColumnInfo(name = "phonetic_us") val phoneticUs: String,
    @ColumnInfo(name = "phonetic_uk") val phoneticUk: String,
    @ColumnInfo(name = "part_of_speech") val partOfSpeech: String,
    val level: String?,
    val meanings: List<MeaningDb>,
    val examples: List<ExampleDb>,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val note: String,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "is_user_added") val isUserAdded: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "last_viewed_at") val lastViewedAt: Long?,
    @ColumnInfo(name = "frequency_rank", defaultValue = "0") val frequencyRank: Int,
)

@Serializable
data class MeaningDb(val translation: String, val description: String = "")

@Serializable
data class ExampleDb(val english: String, val persian: String = "")
