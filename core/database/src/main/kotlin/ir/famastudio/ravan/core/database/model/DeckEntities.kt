package ir.famastudio.ravan.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "color_index") val colorIndex: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(
    tableName = "deck_words",
    primaryKeys = ["deck_id", "word_id"],
    foreignKeys = [
        ForeignKey(entity = DeckEntity::class, parentColumns = ["id"], childColumns = ["deck_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = WordEntity::class, parentColumns = ["id"], childColumns = ["word_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["word_id"])],
)
data class DeckWordCrossRef(
    @ColumnInfo(name = "deck_id") val deckId: String,
    @ColumnInfo(name = "word_id") val wordId: String,
)

data class DeckWithCount(
    @Embedded val deck: DeckEntity,
    @ColumnInfo(name = "word_count") val wordCount: Int,
)
