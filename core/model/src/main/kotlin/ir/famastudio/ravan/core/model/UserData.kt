package ir.famastudio.ravan.core.model

data class UserData(
    val themeConfig: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = false,
    val defaultAccent: Accent = Accent.US,
    val speechRate: Float = 0.9f,
    val autoPronounce: Boolean = true,
    val reviewDirection: ReviewDirection = ReviewDirection.ENGLISH_TO_PERSIAN,
    val dailyGoal: Int = 20,
)

enum class ThemeConfig { FOLLOW_SYSTEM, LIGHT, DARK }

/** Filters of the personal library. The full dictionary is reached through search. */
enum class WordFilter { RECENT, FAVORITES, MINE, IN_REVIEW }

/** Result of a backup export or import. */
data class BackupSummary(
    val words: Int,
    val cards: Int,
    val decks: Int,
    val reviewLogs: Int,
)
