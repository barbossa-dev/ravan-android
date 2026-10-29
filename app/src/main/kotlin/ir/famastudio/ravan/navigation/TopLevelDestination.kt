package ir.famastudio.ravan.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import ir.famastudio.ravan.R
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.feature.home.navigation.HomeRoute
import ir.famastudio.ravan.feature.library.navigation.LibraryRoute
import ir.famastudio.ravan.feature.review.navigation.ReviewRoute
import ir.famastudio.ravan.feature.settings.navigation.SettingsRoute

/** The four destinations shown in the navigation bar / rail. */
enum class TopLevelDestination(
    val icon: ImageVector,
    @StringRes val label: Int,
    val route: Any,
) {
    HOME(RavanIcons.Search, R.string.nav_home, HomeRoute),
    LIBRARY(RavanIcons.Library, R.string.nav_library, LibraryRoute),
    REVIEW(RavanIcons.Review, R.string.nav_review, ReviewRoute),
    SETTINGS(RavanIcons.Settings, R.string.nav_settings, SettingsRoute),
}
