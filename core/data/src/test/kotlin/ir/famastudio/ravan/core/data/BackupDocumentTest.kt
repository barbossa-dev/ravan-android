package ir.famastudio.ravan.core.data

import ir.famastudio.ravan.core.data.backup.BackupDocument
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupDocumentTest {
    private val json = Json { ignoreUnknownKeys = true }

    /** A minimal document as written by the iOS app must be readable on Android. */
    @Test
    fun decodesCrossPlatformDocument() {
        val raw = """
            {"app":"ravan","formatVersion":1,"exportedAt":1700000000000,"platform":"ios",
             "words":[{"id":"seed-water","text":"water","meanings":[{"translation":"آب"}]}],
             "cards":[{"wordId":"seed-water","box":2,"dueAt":1700000000000}],
             "decks":[{"id":"d1","name":"Food","wordIds":["seed-water"]}],
             "reviewLogs":[]}
        """.trimIndent()
        val document = json.decodeFromString(BackupDocument.serializer(), raw)
        assertEquals("water", document.words.single().text)
        assertEquals(2, document.cards.single().box)
        assertEquals(listOf("seed-water"), document.decks.single().wordIds)
    }
}
