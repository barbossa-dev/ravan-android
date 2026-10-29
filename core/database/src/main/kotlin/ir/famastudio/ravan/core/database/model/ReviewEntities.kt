package ir.famastudio.ravan.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "review_cards",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["due_at"])],
)
data class ReviewCardEntity(
    @PrimaryKey @ColumnInfo(name = "word_id") val wordId: String,
    val box: Int,
    @ColumnInfo(name = "due_at") val dueAt: Long,
    @ColumnInfo(name = "review_count") val reviewCount: Int,
    @ColumnInfo(name = "correct_count") val correctCount: Int,
    @ColumnInfo(name = "last_reviewed_at") val lastReviewedAt: Long?,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)

/** A card joined with its word. */
data class PopulatedReviewCard(
    @Embedded val card: ReviewCardEntity,
    @Relation(parentColumn = "word_id", entityColumn = "id")
    val word: WordEntity,
)

/** Review history; kept even if a word is deleted so streaks stay correct. */
@Entity(tableName = "review_logs", indices = [Index(value = ["epoch_day"])])
data class ReviewLogEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "word_id") val wordId: String,
    @ColumnInfo(name = "reviewed_at") val reviewedAt: Long,
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    val correct: Boolean,
)

/** Daily aggregate produced by SQL for the activity chart. */
data class DailyActivityRow(
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    val reviewed: Int,
    val correct: Int,
)
