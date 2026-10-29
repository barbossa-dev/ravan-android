package ir.famastudio.ravan.core.designsystem.component

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection

/**
 * English text inside the RTL interface. Punctuation stays on the correct side and,
 * when [alignToStart] is false, the text hugs the left edge like an English book.
 */
@Composable
fun EnglishText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    alignToStart: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
) {
    if (alignToStart) {
        Text(
            text = text,
            modifier = modifier,
            style = style.copy(textDirection = TextDirection.Ltr),
            color = color,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    } else {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = text,
                modifier = modifier,
                style = style.copy(textDirection = TextDirection.Ltr),
                color = color,
                textAlign = TextAlign.Start,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
