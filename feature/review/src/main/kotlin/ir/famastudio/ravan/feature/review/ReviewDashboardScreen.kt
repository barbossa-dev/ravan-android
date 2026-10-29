package ir.famastudio.ravan.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.core.designsystem.component.BarChart
import ir.famastudio.ravan.core.designsystem.component.BarEntry
import ir.famastudio.ravan.core.designsystem.component.EmptyState
import ir.famastudio.ravan.core.designsystem.component.SectionHeader
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.designsystem.theme.deckColor
import ir.famastudio.ravan.core.model.Deck
import ir.famastudio.ravan.core.model.Leitner
import ir.famastudio.ravan.core.model.ReviewStats
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
internal fun ReviewDashboardScreen(
    onStartSession: (String?, Boolean) -> Unit,
    onBrowseDictionary: () -> Unit,
    viewModel: ReviewDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReviewDashboardContent(uiState, onStartSession, onBrowseDictionary)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewDashboardContent(
    uiState: ReviewDashboardUiState,
    onStartSession: (String?, Boolean) -> Unit,
    onBrowseDictionary: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.feature_review_title)) }) }) { padding ->
        when (uiState) {
            ReviewDashboardUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is ReviewDashboardUiState.Success -> {
                val stats = uiState.stats
                if (stats.totalCards == 0) {
                    EmptyState(
                        icon = RavanIcons.Review,
                        title = stringResource(R.string.feature_review_empty_title),
                        message = stringResource(R.string.feature_review_empty_body),
                        actionLabel = stringResource(R.string.feature_review_browse),
                        onAction = onBrowseDictionary,
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding() + 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item(key = "hero") { HeroCard(stats, onStartSession) }
                        item(key = "tiles") { StatTiles(stats) }
                        item(key = "boxes") { BoxesCard(stats) }
                        item(key = "week") { WeekCard(stats) }
                        if (uiState.decks.isNotEmpty()) {
                            item(key = "decks-header") { SectionHeader(stringResource(R.string.feature_review_decks)) }
                            items(uiState.decks, key = { it.id }) { deck -> DeckRow(deck, onStartSession) }
                        }
                        item(key = "how") { HowItWorks() }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(stats: ReviewStats, onStartSession: (String?, Boolean) -> Unit) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Column(Modifier.padding(24.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.feature_review_due_label), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.feature_review_due_count, stats.dueCount.toPersianDigits()),
                style = MaterialTheme.typography.displayMedium,
            )
            if (stats.dueCount > 0) {
                Button(
                    onClick = { onStartSession(null, false) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(RavanIcons.Play, contentDescription = null)
                    Text(stringResource(R.string.feature_review_start), modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                Text(stringResource(R.string.feature_review_all_done), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    onClick = { onStartSession(null, true) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) { Text(stringResource(R.string.feature_review_practice)) }
            }
        }
    }
}

@Composable
private fun StatTiles(stats: ReviewStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile(
            icon = RavanIcons.Streak,
            iconTint = MaterialTheme.colorScheme.tertiary,
            value = stats.streakDays.toPersianDigits(),
            label = stringResource(R.string.feature_review_streak),
            modifier = Modifier.weight(1f),
        )
        StatTile(
            icon = RavanIcons.Review,
            iconTint = MaterialTheme.colorScheme.primary,
            value = stringResource(
                R.string.feature_review_today_value,
                stats.reviewedToday.toPersianDigits(),
                stats.dailyGoal.toPersianDigits(),
            ),
            label = stringResource(R.string.feature_review_today),
            modifier = Modifier.weight(1f),
            progress = (stats.reviewedToday / stats.dailyGoal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f),
        )
        StatTile(
            icon = RavanIcons.Trophy,
            iconTint = MaterialTheme.colorScheme.secondary,
            value = stats.masteredCount.toPersianDigits(),
            label = stringResource(R.string.feature_review_mastered),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    progress: Float? = null,
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint)
            Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (progress != null) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun BoxesCard(stats: ReviewStats) {
    val mastered = stringResource(R.string.feature_review_box_mastered)
    val entries = stats.boxCounts.mapIndexed { index, count ->
        BarEntry(
            label = if (index + 1 > Leitner.BOX_COUNT) mastered else (index + 1).toPersianDigits(),
            value = count,
            highlighted = index + 1 > Leitner.BOX_COUNT,
        )
    }
    DashboardCard(title = stringResource(R.string.feature_review_boxes)) {
        BarChart(entries = entries, contentDescription = stringResource(R.string.feature_review_boxes_description))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            stats.boxCounts.forEach { count ->
                Text(count.toPersianDigits(), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun WeekCard(stats: ReviewStats) {
    val today = stats.lastSevenDays.lastOrNull()?.epochDay
    val entries = stats.lastSevenDays.map { day ->
        BarEntry(label = persianWeekdayInitial(day.epochDay), value = day.reviewed, highlighted = day.epochDay == today)
    }
    DashboardCard(title = stringResource(R.string.feature_review_week)) {
        BarChart(entries = entries, contentDescription = stringResource(R.string.feature_review_week_description))
        Text(
            stringResource(R.string.feature_review_accuracy, (stats.accuracy * 100).roundToInt().toPersianDigits()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DashboardCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun DeckRow(deck: Deck, onStartSession: (String?, Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(deckColor(deck.colorIndex)),
                )
            },
            headlineContent = { Text(deck.name) },
            supportingContent = { Text(stringResource(R.string.feature_review_deck_count, deck.wordCount.toPersianDigits())) },
            trailingContent = {
                FilledTonalButton(onClick = { onStartSession(deck.id, false) }) { Text(stringResource(R.string.feature_review_start)) }
            },
        )
    }
}

@Composable
private fun HowItWorks() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(RavanIcons.Tip, contentDescription = null)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.feature_review_how_title), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.feature_review_how_body), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun persianWeekdayInitial(epochDay: Long): String = when (LocalDate.ofEpochDay(epochDay).dayOfWeek) {
    DayOfWeek.SATURDAY -> "ش"
    DayOfWeek.SUNDAY -> "ی"
    DayOfWeek.MONDAY -> "د"
    DayOfWeek.TUESDAY -> "س"
    DayOfWeek.WEDNESDAY -> "چ"
    DayOfWeek.THURSDAY -> "پ"
    DayOfWeek.FRIDAY -> "ج"
}
