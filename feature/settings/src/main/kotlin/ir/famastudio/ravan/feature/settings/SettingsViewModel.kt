package ir.famastudio.ravan.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.famastudio.ravan.core.data.repository.BackupRepository
import ir.famastudio.ravan.core.data.repository.UserDataRepository
import ir.famastudio.ravan.core.data.repository.WordRepository
import ir.famastudio.ravan.core.model.Accent
import ir.famastudio.ravan.core.model.BackupSummary
import ir.famastudio.ravan.core.model.ReviewDirection
import ir.famastudio.ravan.core.model.ThemeConfig
import ir.famastudio.ravan.core.model.UserData
import ir.famastudio.ravan.core.speech.Pronouncer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val userData: UserData, val wordCount: Int) : SettingsUiState
}

sealed interface BackupStatus {
    data object Working : BackupStatus
    data class Exported(val summary: BackupSummary) : BackupStatus
    data class Imported(val summary: BackupSummary) : BackupStatus
    data object Failed : BackupStatus
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val backupRepository: BackupRepository,
    private val pronouncer: Pronouncer,
    wordRepository: WordRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userDataRepository.userData,
        wordRepository.observeWordCount(),
    ) { userData, count -> SettingsUiState.Success(userData, count) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    private val _backupStatus = MutableStateFlow<BackupStatus?>(null)
    val backupStatus: StateFlow<BackupStatus?> = _backupStatus.asStateFlow()

    fun setTheme(value: ThemeConfig) = launch { userDataRepository.setThemeConfig(value) }
    fun setDynamicColor(value: Boolean) = launch { userDataRepository.setDynamicColor(value) }
    fun setAccent(value: Accent) = launch { userDataRepository.setDefaultAccent(value) }
    fun setSpeechRate(value: Float) = launch { userDataRepository.setSpeechRate(value) }
    fun setAutoPronounce(value: Boolean) = launch { userDataRepository.setAutoPronounce(value) }
    fun setReviewDirection(value: ReviewDirection) = launch { userDataRepository.setReviewDirection(value) }
    fun setDailyGoal(value: Int) = launch { userDataRepository.setDailyGoal(value) }

    fun testVoice(text: String) {
        val userData = (uiState.value as? SettingsUiState.Success)?.userData ?: UserData()
        pronouncer.speak(text, userData.defaultAccent, userData.speechRate)
    }

    fun export(uri: Uri) = runBackup { BackupStatus.Exported(backupRepository.exportTo(uri)) }

    fun import(uri: Uri) = runBackup { BackupStatus.Imported(backupRepository.importFrom(uri)) }

    fun onBackupStatusShown() {
        _backupStatus.value = null
    }

    private fun runBackup(block: suspend () -> BackupStatus) {
        _backupStatus.value = BackupStatus.Working
        viewModelScope.launch {
            _backupStatus.value = try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                BackupStatus.Failed
            }
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
