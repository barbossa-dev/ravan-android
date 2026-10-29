package ir.famastudio.ravan.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import ir.famastudio.ravan.core.model.Accent
import ir.famastudio.ravan.core.model.ReviewDirection
import ir.famastudio.ravan.core.model.ThemeConfig
import ir.famastudio.ravan.core.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/** Single source of truth for user settings. */
class RavanPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val preferences: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val userData: Flow<UserData> = preferences.map { prefs ->
        val defaults = UserData()
        UserData(
            themeConfig = prefs[Keys.THEME].toEnum(defaults.themeConfig),
            useDynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: defaults.useDynamicColor,
            defaultAccent = prefs[Keys.ACCENT].toEnum(defaults.defaultAccent),
            speechRate = prefs[Keys.SPEECH_RATE] ?: defaults.speechRate,
            autoPronounce = prefs[Keys.AUTO_PRONOUNCE] ?: defaults.autoPronounce,
            reviewDirection = prefs[Keys.REVIEW_DIRECTION].toEnum(defaults.reviewDirection),
            dailyGoal = prefs[Keys.DAILY_GOAL] ?: defaults.dailyGoal,
        )
    }

    suspend fun seedVersion(): Int = preferences.first()[Keys.SEED_VERSION] ?: 0

    suspend fun setSeedVersion(version: Int) = dataStore.edit { it[Keys.SEED_VERSION] = version }
    suspend fun setThemeConfig(value: ThemeConfig) = dataStore.edit { it[Keys.THEME] = value.name }
    suspend fun setDynamicColor(value: Boolean) = dataStore.edit { it[Keys.DYNAMIC_COLOR] = value }
    suspend fun setDefaultAccent(value: Accent) = dataStore.edit { it[Keys.ACCENT] = value.name }
    suspend fun setSpeechRate(value: Float) = dataStore.edit { it[Keys.SPEECH_RATE] = value }
    suspend fun setAutoPronounce(value: Boolean) = dataStore.edit { it[Keys.AUTO_PRONOUNCE] = value }
    suspend fun setReviewDirection(value: ReviewDirection) = dataStore.edit { it[Keys.REVIEW_DIRECTION] = value.name }
    suspend fun setDailyGoal(value: Int) = dataStore.edit { it[Keys.DAILY_GOAL] = value }

    private object Keys {
        val THEME = stringPreferencesKey("theme_config")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ACCENT = stringPreferencesKey("default_accent")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val AUTO_PRONOUNCE = booleanPreferencesKey("auto_pronounce")
        val REVIEW_DIRECTION = stringPreferencesKey("review_direction")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val SEED_VERSION = intPreferencesKey("seed_version")
    }
}

private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
    this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
