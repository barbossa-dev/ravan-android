package ir.famastudio.ravan.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BarEntry(val label: String, val value: Int, val highlighted: Boolean = false)

/** Minimal animated bar chart (no chart library needed for seven bars). */
@Composable
fun BarChart(
    entries: List<BarEntry>,
    contentDescription: String,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 120.dp,
) {
    val max = (entries.maxOfOrNull { it.value } ?: 0).coerceAtLeast(1)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        entries.forEach { entry ->
            val fraction by animateFloatAsState(entry.value / max.toFloat(), label = "bar")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.height(chartHeight), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .width(22.dp)
                            .fillMaxHeight(fraction.coerceAtLeast(0.04f))
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(
                                if (entry.highlighted) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            ),
                    )
                }
                Text(
                    entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
