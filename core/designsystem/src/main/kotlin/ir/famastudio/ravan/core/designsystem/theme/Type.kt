package ir.famastudio.ravan.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val base = Typography()

/** Persian text needs a little more line height than Latin for its descenders and dots. */
internal val RavanTypography = Typography(
    displayLarge = base.displayLarge,
    displayMedium = base.displayMedium,
    displaySmall = base.displaySmall,
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold, lineHeight = 26.sp),
    titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(lineHeight = 28.sp),
    bodyMedium = base.bodyMedium.copy(lineHeight = 24.sp),
    bodySmall = base.bodySmall.copy(lineHeight = 20.sp),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium,
    labelSmall = base.labelSmall,
)

/** English headwords and sentences use a serif so they stand apart from Persian at a glance. */
object EnglishTextStyles {
    val headwordLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 48.sp)
    val headwordMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp)
    val headwordSmall = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 24.sp)
    val sentence = TextStyle(fontFamily = FontFamily.Serif, fontSize = 17.sp, lineHeight = 25.sp)
    val phonetic = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp)
}
