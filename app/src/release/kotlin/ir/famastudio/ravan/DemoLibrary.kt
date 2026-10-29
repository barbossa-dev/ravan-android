package ir.famastudio.ravan

import android.content.Context
import ir.famastudio.ravan.core.data.repository.BackupRepository
import ir.famastudio.ravan.core.data.repository.WordRepository

/** Release builds never install sample data; see the debug source set. */
internal object DemoLibrary {
    @Suppress("UNUSED_PARAMETER", "RedundantSuspendModifier")
    suspend fun installOnce(context: Context, words: WordRepository, backup: BackupRepository) = Unit
}
