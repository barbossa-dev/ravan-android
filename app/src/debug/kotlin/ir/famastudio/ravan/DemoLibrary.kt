package ir.famastudio.ravan

import android.content.Context
import android.net.Uri
import ir.famastudio.ravan.core.data.repository.BackupRepository
import ir.famastudio.ravan.core.data.repository.WordRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Debug builds only: installs a realistic few weeks of study once (review cards in every
 * Leitner box, seven days of review logs, decks, recently viewed and favourite words) so the
 * store screenshots show a lived-in app. It goes through the normal backup import, and uses
 * the same data as the iOS `DemoLibrary`. Release builds get a no-op (see src/release).
 */
internal object DemoLibrary {
    private const val PREFS = "ravan_debug"
    private const val KEY = "demo_library_v1"

    private val boxes = listOf(
        1 to listOf("achieve", "journey", "abundant", "eager", "reliable", "confident", "improve", "vocabulary", "habit", "practice"),
        2 to listOf("resilient", "curious", "brave", "fluent", "memory", "challenge", "opportunity", "knowledge", "patient", "honest"),
        3 to listOf("ambitious", "generous", "grateful", "obvious", "ancient", "efficient", "essential", "flexible"),
        4 to listOf("accurate", "anxious", "brilliant", "capable", "diverse", "genuine"),
        5 to listOf("inevitable", "meticulous", "persistent", "profound"),
        6 to listOf("apple", "house", "water", "friend", "happy", "family", "travel"),
    )
    private val recentlyViewed = listOf("journey", "achieve", "reliable", "eager", "curious", "resilient", "brave")
    private val favorites = listOf("resilient", "brave", "fluent", "journey")
    private val logsPerDay = listOf(14, 22, 9, 18, 25, 16, 11) // six days ago … today
    private val intervalDays = listOf(1, 2, 4, 8, 16, 60)

    private fun id(text: String) = "seed-" + text.lowercase().replace(' ', '-')

    suspend fun installOnce(context: Context, words: WordRepository, backup: BackupRepository) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY, false)) return
        if (words.getWord(id("achieve")) == null) return // dictionary not imported yet

        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 24 * hour

        val cards = JSONArray()
        boxes.forEach { (box, texts) ->
            texts.forEachIndexed { index, text ->
                val due = when {
                    box == 1 -> now - day + index * 60_000L
                    box == 2 && index < 3 -> now - hour
                    else -> now + intervalDays[box - 1] * day - index * 3 * hour
                }
                val reviews = maxOf(0, (box - 1) * 2 + index % 3)
                cards.put(
                    JSONObject()
                        .put("wordId", id(text))
                        .put("box", box)
                        .put("dueAt", due)
                        .put("reviewCount", reviews)
                        .put("correctCount", maxOf(0, reviews - index % 2))
                        .put("lastReviewedAt", if (box == 1) JSONObject.NULL else now - (index % 3 + 1) * day)
                        .put("addedAt", now - 21 * day),
                )
            }
        }

        val all = boxes.flatMap { it.second }
        val logs = JSONArray()
        logsPerDay.forEachIndexed { offset, count ->
            val daysAgo = (logsPerDay.size - 1 - offset).toLong()
            repeat(count) { k ->
                logs.put(
                    JSONObject()
                        .put("id", "demo-log-$daysAgo-$k")
                        .put("wordId", id(all[(offset * 7 + k) % all.size]))
                        .put("reviewedAt", now - daysAgo * day - k * 90_000L)
                        .put("correct", k % 5 != 0),
                )
            }
        }

        fun deck(deckId: String, name: String, color: Int, texts: List<String>) = JSONObject()
            .put("id", deckId)
            .put("name", name)
            .put("colorIndex", color)
            .put("createdAt", now - 20 * day)
            .put("wordIds", JSONArray(texts.map(::id)))
        val decks = JSONArray()
            .put(deck("demo-deck-ielts", "آیلتس", 0, boxes[1].second + boxes[2].second))
            .put(deck("demo-deck-504", "لغت‌های ۵۰۴", 1, boxes[0].second + boxes[3].second))
            .put(deck("demo-deck-travel", "سفر", 2, listOf("journey", "travel", "abundant", "ancient", "family")))

        val document = JSONObject()
            .put("app", "ravan")
            .put("formatVersion", 1)
            .put("exportedAt", now)
            .put("platform", "android")
            .put("words", JSONArray())
            .put("cards", cards)
            .put("decks", decks)
            .put("reviewLogs", logs)
        val file = File(context.cacheDir, "demo-library.json").apply { writeText(document.toString()) }
        backup.importFrom(Uri.fromFile(file))
        file.delete()

        recentlyViewed.forEach { words.markViewed(id(it)) }
        favorites.forEach { words.setFavorite(id(it), true) }
        prefs.edit().putBoolean(KEY, true).apply()
    }
}
