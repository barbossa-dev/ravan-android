package ir.famastudio.ravan.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.famastudio.ravan.core.database.dao.DeckDao
import ir.famastudio.ravan.core.database.dao.ReviewDao
import ir.famastudio.ravan.core.database.dao.WordDao
import ir.famastudio.ravan.core.database.model.DeckEntity
import ir.famastudio.ravan.core.database.model.DeckWordCrossRef
import ir.famastudio.ravan.core.database.model.ReviewCardEntity
import ir.famastudio.ravan.core.database.model.ReviewLogEntity
import ir.famastudio.ravan.core.database.model.WordEntity
import ir.famastudio.ravan.core.database.util.RavanConverters

@Database(
    entities = [
        WordEntity::class,
        ReviewCardEntity::class,
        ReviewLogEntity::class,
        DeckEntity::class,
        DeckWordCrossRef::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RavanConverters::class)
internal abstract class RavanDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun reviewDao(): ReviewDao
    abstract fun deckDao(): DeckDao
}
