package ir.famastudio.ravan.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.core.model.ReviewCard
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

@Composable
fun boxLabel(card: ReviewCard): String =
    if (card.isMastered) stringResource(R.string.core_ui_mastered)
    else stringResource(R.string.core_ui_box, card.box.toPersianDigits())

@Composable
fun dueLabel(card: ReviewCard, now: Long): String {
    val remaining = card.dueAt - now
    if (remaining <= 0) return stringResource(R.string.core_ui_due_now)
    val days = ceil(remaining / TimeUnit.DAYS.toMillis(1).toDouble()).toInt()
    return if (days <= 1) stringResource(R.string.core_ui_due_tomorrow)
    else stringResource(R.string.core_ui_due_in_days, days.toPersianDigits())
}
