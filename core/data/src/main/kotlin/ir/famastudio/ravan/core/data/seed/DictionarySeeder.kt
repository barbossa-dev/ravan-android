package ir.famastudio.ravan.core.data.seed

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.famastudio.ravan.core.common.di.Dispatcher
import ir.famastudio.ravan.core.common.di.RavanDispatchers
import ir.famastudio.ravan.core.data.model.asEntity
import ir.famastudio.ravan.core.data.model.seedWordId
import ir.famastudio.ravan.core.database.dao.WordDao
import ir.famastudio.ravan.core.datastore.RavanPreferencesDataSource
import ir.famastudio.ravan.core.model.CefrLevel
import ir.famastudio.ravan.core.model.Example
import ir.famastudio.ravan.core.model.Meaning
import ir.famastudio.ravan.core.model.PartOfSpeech
import ir.famastudio.ravan.core.model.Word
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the bundled offline dictionary (~21k entries, see tools/dictionary) into Room on first
 * launch and whenever a newer dictionary ships. Existing rows are never overwritten, so user
 * edits survive updates.
 */
@Singleton
class DictionarySeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordDao: WordDao,
    private val preferences: RavanPreferencesDataSource,
    private val clock: Clock,
    @Dispatcher(RavanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun seedIfNeeded() = withContext(ioDispatcher) {
        if (preferences.seedVersion() >= SEED_VERSION) return@withContext
        val dictionary = context.assets.open(ASSET_NAME).bufferedReader().use { reader ->
            json.decodeFromString(SeedDictionary.serializer(), reader.readText())
        }
        val now = clock.millis()
        dictionary.words.asSequence()
            .map { it.asWord(now).asEntity() }
            .chunked(BATCH_SIZE)
            .forEach { batch -> wordDao.insertAllIgnoringExisting(batch) }
        preferences.setSeedVersion(dictionary.version)
    }

    private fun SeedWord.asWord(now: Long) = Word(
        id = seedWordId(text),
        text = text,
        phoneticUs = phoneticUs,
        phoneticUk = phoneticUk,
        partOfSpeech = PartOfSpeech.fromKey(partOfSpeech),
        level = CefrLevel.fromKey(level),
        meanings = meanings.map { Meaning(it.translation, it.description) },
        examples = examples.map { Example(it.english, it.persian) },
        synonyms = synonyms,
        antonyms = antonyms,
        frequencyRank = rank,
        isUserAdded = false,
        createdAt = now,
        updatedAt = now,
    )

    private companion object {
        const val ASSET_NAME = "dictionary.json"
        const val SEED_VERSION = 2
        const val BATCH_SIZE = 1_000
        val json = Json { ignoreUnknownKeys = true }
    }
}
