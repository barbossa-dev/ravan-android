package ir.famastudio.ravan.core.data.repository

import android.net.Uri
import ir.famastudio.ravan.core.model.BackupSummary

/** Exports the whole library (words, Leitner cards, decks, history) to a JSON file and restores it. */
interface BackupRepository {
    suspend fun exportTo(uri: Uri): BackupSummary

    /** Merges a backup into the current data: entries with the same id are replaced, others are kept. */
    suspend fun importFrom(uri: Uri): BackupSummary
}
