package ir.famastudio.ravan.core.data.repository

import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.core.model.WordFilter
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun observeWords(filter: WordFilter): Flow<List<Word>>
    fun observeWord(id: String): Flow<Word?>
    fun search(query: String, limit: Int = 60): Flow<List<Word>>
    fun observeRecentWords(limit: Int = 8): Flow<List<Word>>
    fun observeWordOfTheDay(epochDay: Long): Flow<Word?>
    fun observeWordCount(): Flow<Int>

    suspend fun getWord(id: String): Word?
    suspend fun findByText(text: String): Word?

    /** Maps each of [texts] that exists in the dictionary to its word id. */
    suspend fun findIdsByTexts(texts: List<String>): Map<String, String>

    /** Inserts (blank id) or updates a word and returns its id. */
    suspend fun saveWord(word: Word): String
    suspend fun setFavorite(id: String, favorite: Boolean)
    suspend fun markViewed(id: String)
    suspend fun deleteWord(id: String)
}
