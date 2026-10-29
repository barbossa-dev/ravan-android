package ir.famastudio.ravan.core.data.repository

import ir.famastudio.ravan.core.datastore.RavanPreferencesDataSource
import ir.famastudio.ravan.core.model.Accent
import ir.famastudio.ravan.core.model.ReviewDirection
import ir.famastudio.ravan.core.model.ThemeConfig
import ir.famastudio.ravan.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserDataRepository {
    val userData: Flow<UserData>
    suspend fun setThemeConfig(themeConfig: ThemeConfig)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setDefaultAccent(accent: Accent)
    suspend fun setSpeechRate(rate: Float)
    suspend fun setAutoPronounce(enabled: Boolean)
    suspend fun setReviewDirection(direction: ReviewDirection)
    suspend fun setDailyGoal(goal: Int)
}

internal class OfflineUserDataRepository @Inject constructor(
    private val preferences: RavanPreferencesDataSource,
) : UserDataRepository {
    override val userData: Flow<UserData> = preferences.userData
    override suspend fun setThemeConfig(themeConfig: ThemeConfig) { preferences.setThemeConfig(themeConfig) }
    override suspend fun setDynamicColor(enabled: Boolean) { preferences.setDynamicColor(enabled) }
    override suspend fun setDefaultAccent(accent: Accent) { preferences.setDefaultAccent(accent) }
    override suspend fun setSpeechRate(rate: Float) { preferences.setSpeechRate(rate) }
    override suspend fun setAutoPronounce(enabled: Boolean) { preferences.setAutoPronounce(enabled) }
    override suspend fun setReviewDirection(direction: ReviewDirection) { preferences.setReviewDirection(direction) }
    override suspend fun setDailyGoal(goal: Int) { preferences.setDailyGoal(goal) }
}
