package ir.famastudio.ravan.feature.review.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import ir.famastudio.ravan.feature.review.ReviewDashboardScreen
import ir.famastudio.ravan.feature.review.session.ReviewSessionScreen
import kotlinx.serialization.Serializable

@Serializable
data object ReviewRoute

/** A flash-card session. `practice = true` shows cards that are not due and keeps Leitner boxes unchanged. */
@Serializable
data class ReviewSessionRoute(
    val deckId: String? = null,
    val practice: Boolean = false,
)

fun NavController.navigateToReview(navOptions: NavOptions? = null) = navigate(ReviewRoute, navOptions)

fun NavController.navigateToReviewSession(deckId: String? = null, practice: Boolean = false) =
    navigate(ReviewSessionRoute(deckId, practice))

fun NavGraphBuilder.reviewScreen(
    onStartSession: (deckId: String?, practice: Boolean) -> Unit,
    onBrowseDictionary: () -> Unit,
) {
    composable<ReviewRoute> {
        ReviewDashboardScreen(onStartSession = onStartSession, onBrowseDictionary = onBrowseDictionary)
    }
}

fun NavGraphBuilder.reviewSessionScreen(
    onClose: () -> Unit,
    onWordClick: (String) -> Unit,
) {
    composable<ReviewSessionRoute> {
        ReviewSessionScreen(onClose = onClose, onWordClick = onWordClick)
    }
}
