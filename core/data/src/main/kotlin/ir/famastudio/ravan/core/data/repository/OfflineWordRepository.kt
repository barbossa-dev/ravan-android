package ir.famastudio.ravan.core.data.repository

import ir.famastudio.ravan.core.common.text.SearchNormalizer
import ir.famastudio.ravan.core.data.model.asEntity
import ir.famastudio.ravan.core.data.model.asExternalModel
import ir.famastudio.ravan.core.database.dao.WordDao
import ir.famastudio.ravan.core.database.model.WordEntity
import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.core.model.WordFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

internal class OfflineWordRepository @Inject constructor(
    private val wordDao: WordDao,
    private val clock: Clock,
) : WordRepository {

    override fun observeWords(filter: WordFilter): Flow<List<Word>> = when (filter) {
        WordFilter.RECENT -> wordDao.observeRecent(limit = RECENT_LIMIT)
        WordFilter.FAVORITES -> wordDao.observeFavorites()
        WordFilter.MINE -> wordDao.observeUserAdded()
        WordFilter.IN_REVIEW -> wordDao.observeInReview()
    }.mapToModels()

    override fun observeWord(id: String): Flow<Word?> =
        wordDao.observeById(id).map { it?.asExternalModel() }

    override fun search(query: String, limit: Int): Flow<List<Word>> {
        val normalized = SearchNormalizer.normalize(query)
        if (normalized.isEmpty()) return flowOf(emptyList())
        // Escape LIKE wildcards the user may type.
        val safe = normalized.replace("%", "").replace("_", "")
        return wordDao.search(safe, limit).mapToModels()
    }

    override fun observeRecentWords(limit: Int): Flow<List<Word>> =
        wordDao.observeRecent(limit).mapToModels()

    override fun observeWordOfTheDay(epochDay: Long): Flow<Word?> =
        wordDao.observeWordOfTheDayPoolSize().flatMapLatest { size ->
            if (size == 0) {
                flowOf(null)
            } else {
                // A fixed stride spreads consecutive days across the alphabet.
                val offset = Math.floorMod(epochDay * WORD_OF_DAY_STRIDE, size.toLong()).toInt()
                wordDao.observeWordOfTheDayAt(offset).map { it?.asExternalModel() }
            }
        }

    override fun observeWordCount(): Flow<Int> = wordDao.observeCount()

    override suspend fun getWord(id: String): Word? = wordDao.getById(id)?.asExternalModel()

    override suspend fun findByText(text: String): Word? =
        wordDao.getByNormalizedText(SearchNormalizer.normalize(text))?.asExternalModel()

    override suspend fun findIdsByTexts(texts: List<String>): Map<String, String> {
        if (texts.isEmpty()) return emptyMap()
        val byNormalized = texts.associateBy { SearchNormalizer.normalize(it) }
        return wordDao.getByNormalizedTexts(byNormalized.keys.toList())
            .mapNotNull { entity -> byNormalized[entity.textLower]?.let { original -> original to entity.id } }
            .toMap()
    }

    override suspend fun saveWord(word: Word): String {
        val now = clock.millis()
        val toSave = if (word.id.isBlank()) {
            word.copy(id = UUID.randomUUID().toString(), isUserAdded = true, createdAt = now, updatedAt = now)
        } else {
            word.copy(updatedAt = now)
        }
        wordDao.upsert(toSave.asEntity())
        return toSave.id
    }

    override suspend fun setFavorite(id: String, favorite: Boolean) =
        wordDao.setFavorite(id, favorite, clock.millis())

    override suspend fun markViewed(id: String) = wordDao.markViewed(id, clock.millis())

    override suspend fun deleteWord(id: String) = wordDao.delete(id)

    private companion object {
        const val RECENT_LIMIT = 200
        const val WORD_OF_DAY_STRIDE = 7_919L
    }

    private fun Flow<List<WordEntity>>.mapToModels(): Flow<List<Word>> =
        map { entities -> entities.map(WordEntity::asExternalModel) }
}
