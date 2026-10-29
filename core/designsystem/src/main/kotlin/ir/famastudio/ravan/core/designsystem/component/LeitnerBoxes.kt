package ir.famastudio.ravan.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Five segments; the first [box] are filled. A mastered card fills all of them in the accent colour. */
@Composable
fun LeitnerProgress(
    box: Int,
    boxCount: Int,
    modifier: Modifier = Modifier,
) {
    val mastered = box > boxCount
    Row(modifier = modifier.height(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(boxCount) { index ->
            val color = when {
                mastered -> MaterialTheme.colorScheme.tertiary
                index < box -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceContainerHighest
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
            )
        }
    }
}
