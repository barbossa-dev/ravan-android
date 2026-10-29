package ir.famastudio.ravan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.famastudio.ravan.core.data.repository.ReviewRepository
import ir.famastudio.ravan.core.data.repository.UserDataRepository
import ir.famastudio.ravan.core.model.ThemeConfig
import ir.famastudio.ravan.core.model.UserData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import javax.inject.Inject

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val userData: UserData) : MainActivityUiState {
        fun shouldUseDarkTheme(isSystemInDarkTheme: Boolean): Boolean = when (userData.themeConfig) {
            ThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme
            ThemeConfig.LIGHT -> false
            ThemeConfig.DARK -> true
        }
    }
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    reviewRepository: ReviewRepository,
    clock: Clock,
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = userDataRepository.userData
        .map<UserData, MainActivityUiState> { MainActivityUiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainActivityUiState.Loading)

    /** Shown as a badge on the review tab. */
    val dueCount: StateFlow<Int> = reviewRepository.observeCards()
        .map { cards ->
            val now = clock.millis()
            cards.count { it.isDue(now) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
