package ir.famastudio.ravan.core.database

import androidx.room.withTransaction
import javax.inject.Inject

/** Lets the data layer run several DAO calls atomically without seeing the database class. */
interface TransactionRunner {
    suspend fun <R> run(block: suspend () -> R): R
}

internal class RoomTransactionRunner @Inject constructor(
    private val database: RavanDatabase,
) : TransactionRunner {
    override suspend fun <R> run(block: suspend () -> R): R = database.withTransaction { block() }
}
