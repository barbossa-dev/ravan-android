package ir.famastudio.ravan.core.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.famastudio.ravan.core.common.di.Dispatcher
import ir.famastudio.ravan.core.common.di.RavanDispatchers
import ir.famastudio.ravan.core.data.backup.BackupDocument
import ir.famastudio.ravan.core.data.backup.BackupWord
import ir.famastudio.ravan.core.data.backup.InvalidBackupException
import ir.famastudio.ravan.core.data.backup.asBackup
import ir.famastudio.ravan.core.data.backup.asDb
import ir.famastudio.ravan.core.data.backup.asEntity
import ir.famastudio.ravan.core.data.model.asEntity
import ir.famastudio.ravan.core.data.model.asExternalModel
import ir.famastudio.ravan.core.database.TransactionRunner
import ir.famastudio.ravan.core.database.dao.DeckDao
import ir.famastudio.ravan.core.database.dao.ReviewDao
import ir.famastudio.ravan.core.database.dao.WordDao
import ir.famastudio.ravan.core.database.model.DeckEntity
import ir.famastudio.ravan.core.database.model.DeckWordCrossRef
import ir.famastudio.ravan.core.database.model.ReviewLogEntity
import ir.famastudio.ravan.core.database.model.WordEntity
import ir.famastudio.ravan.core.model.BackupSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

internal class FileBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordDao: WordDao,
    private val reviewDao: ReviewDao,
    private val deckDao: DeckDao,
    private val transaction: TransactionRunner,
    private val clock: Clock,
    @Dispatcher(RavanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : BackupRepository {

    override suspend fun exportTo(uri: Uri): BackupSummary = withContext(ioDispatcher) {
        val crossRefs = deckDao.getCrossRefs().groupBy({ it.deckId }, { it.wordId })
        val document = BackupDocument(
            exportedAt = clock.millis(),
            words = wordDao.getAll().map(WordEntity::asBackup),
            cards = reviewDao.getCards().map { it.asBackup() },
            decks = deckDao.getDecks().map { it.asBackup(crossRefs[it.id].orEmpty()) },
            reviewLogs = reviewDao.getLogs().map { it.asBackup() },
        )
        val output = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw InvalidBackupException("Cannot open $uri for writing")
        output.bufferedWriter().use { it.write(json.encodeToString(BackupDocument.serializer(), document)) }
        document.summary()
    }

    override suspend fun importFrom(uri: Uri): BackupSummary = withContext(ioDispatcher) {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw InvalidBackupException("Cannot open $uri")
        val document = try {
            json.decodeFromString(BackupDocument.serializer(), text)
        } catch (e: SerializationException) {
            throw InvalidBackupException("Not a Ravan backup", e)
        } catch (e: IllegalArgumentException) {
            throw InvalidBackupException("Not a Ravan backup", e)
        }
        if (document.app != BackupDocument.APP_ID || document.formatVersion > BackupDocument.FORMAT_VERSION) {
            throw InvalidBackupException("Unsupported backup: ${document.app} v${document.formatVersion}")
        }

        transaction.run {
            wordDao.upsertAll(document.words.map { it.asEntity() })
            val wordIds = wordDao.getAll().mapTo(HashSet()) { it.id }

            reviewDao.upsertCards(document.cards.filter { it.wordId in wordIds }.map { it.asEntity() })

            deckDao.upsertDecks(document.decks.map { DeckEntity(it.id, it.name, it.colorIndex, it.createdAt) })
            deckDao.insertCrossRefs(
                document.decks.flatMap { deck ->
                    deck.wordIds.filter { it in wordIds }.map { DeckWordCrossRef(deck.id, it) }
                },
            )

            reviewDao.insertLogs(
                document.reviewLogs.map {
                    ReviewLogEntity(
                        id = it.id,
                        wordId = it.wordId,
                        reviewedAt = it.reviewedAt,
                        epochDay = Instant.ofEpochMilli(it.reviewedAt).atZone(clock.zone).toLocalDate().toEpochDay(),
                        correct = it.correct,
                    )
                },
            )
        }
        document.summary()
    }

    /** Goes through the domain model so derived search columns are rebuilt consistently. */
    private fun BackupWord.asEntity(): WordEntity = WordEntity(
        id = id,
        text = text,
        textLower = "",
        searchText = "",
        phoneticUs = phoneticUs,
        phoneticUk = phoneticUk,
        partOfSpeech = partOfSpeech,
        level = level,
        meanings = meanings.map { it.asDb() },
        examples = examples.map { it.asDb() },
        synonyms = synonyms,
        antonyms = antonyms,
        note = note,
        isFavorite = isFavorite,
        isUserAdded = isUserAdded,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastViewedAt = lastViewedAt,
        frequencyRank = frequencyRank,
    ).asExternalModel().asEntity()

    private fun BackupDocument.summary() = BackupSummary(
        words = words.size,
        cards = cards.size,
        decks = decks.size,
        reviewLogs = reviewLogs.size,
    )

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = true
        }
    }
}
