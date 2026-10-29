package ir.famastudio.ravan.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.core.designsystem.component.EmptyState
import ir.famastudio.ravan.core.designsystem.component.RavanSearchField
import ir.famastudio.ravan.core.designsystem.component.SectionHeader
import ir.famastudio.ravan.core.designsystem.component.SpeakIconButton
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.designsystem.theme.RavanTheme
import ir.famastudio.ravan.core.model.Accent
import ir.famastudio.ravan.core.model.Meaning
import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.core.ui.DevicePreviews
import ir.famastudio.ravan.core.ui.WordListItem
import ir.famastudio.ravan.core.ui.WordOfTheDayCard

@Composable
internal fun HomeScreen(
    onWordClick: (String) -> Unit,
    onAddWord: (String) -> Unit,
    onStartReview: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val feedState by viewModel.feedState.collectAsStateWithLifecycle()
    val speakingText by viewModel.speakingText.collectAsStateWithLifecycle()

    HomeContent(
        query = query,
        onQueryChange = viewModel::onQueryChange,
        searchState = searchState,
        feedState = feedState,
        speakingText = speakingText,
        onSpeak = viewModel::speak,
        onWordClick = onWordClick,
        onAddWord = onAddWord,
        onStartReview = onStartReview,
    )
}

@Composable
internal fun HomeContent(
    query: String,
    onQueryChange: (String) -> Unit,
    searchState: SearchUiState,
    feedState: HomeFeedUiState,
    speakingText: String?,
    onSpeak: (Word) -> Unit,
    onWordClick: (String) -> Unit,
    onAddWord: (String) -> Unit,
    onStartReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .statusBarsPadding()
                    .padding(top = 12.dp, bottom = 8.dp),
            ) {
                Text(stringResource(R.string.feature_home_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.feature_home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RavanSearchField(
                    query = query,
                    onQueryChange = onQueryChange,
                    placeholder = stringResource(R.string.feature_home_search_hint),
                    clearContentDescription = stringResource(R.string.feature_home_clear),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddWord(query.trim()) },
                icon = { Icon(RavanIcons.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.feature_home_new_word)) },
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = searchState is SearchUiState.Results,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "home-content",
        ) { searching ->
            if (searching && searchState is SearchUiState.Results) {
                SearchResults(
                    state = searchState,
                    speakingText = speakingText,
                    onSpeak = onSpeak,
                    onWordClick = onWordClick,
                    onAddWord = onAddWord,
                )
            } else {
                HomeFeed(
                    state = feedState,
                    speakingText = speakingText,
                    onSpeak = onSpeak,
                    onWordClick = onWordClick,
                    onStartReview = onStartReview,
                )
            }
        }
    }
}

@Composable
private fun SearchResults(
    state: SearchUiState.Results,
    speakingText: String?,
    onSpeak: (Word) -> Unit,
    onWordClick: (String) -> Unit,
    onAddWord: (String) -> Unit,
) {
    if (state.words.isEmpty()) {
        EmptyState(
            icon = RavanIcons.Search,
            title = stringResource(R.string.feature_home_not_found_title, state.query),
            message = stringResource(R.string.feature_home_not_found_body),
            actionLabel = stringResource(R.string.feature_home_add_to_dictionary),
            onAction = { onAddWord(state.query) },
        )
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        item {
            Text(
                stringResource(R.string.feature_home_results, state.words.size.toPersianDigits()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        items(state.words, key = { it.id }) { word ->
            WordListItem(
                word = word,
                onClick = { onWordClick(word.id) },
                trailingContent = {
                    SpeakIconButton(
                        isSpeaking = speakingText == word.text,
                        onClick = { onSpeak(word) },
                        contentDescription = stringResource(R.string.feature_home_speak),
                    )
                },
            )
        }
    }
}

@Composable
private fun HomeFeed(
    state: HomeFeedUiState,
    speakingText: String?,
    onSpeak: (Word) -> Unit,
    onWordClick: (String) -> Unit,
    onStartReview: () -> Unit,
) {
    when (state) {
        HomeFeedUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is HomeFeedUiState.Success -> LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "review") {
                ReviewCallout(dueCount = state.dueCount, wordCount = state.wordCount, onStartReview = onStartReview)
            }
            state.wordOfTheDay?.let { word ->
                item(key = "wotd") {
                    WordOfTheDayCard(
                        word = word,
                        accent = state.accent,
                        isSpeaking = speakingText == word.text,
                        onSpeak = { onSpeak(word) },
                        onClick = { onWordClick(word.id) },
                    )
                }
            }
            if (state.recentWords.isEmpty()) {
                item(key = "tip") { TipCard() }
            } else {
                item(key = "recent-header") { SectionHeader(stringResource(R.string.feature_home_recent)) }
                item(key = "recent") {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        state.recentWords.forEach { word ->
                            WordListItem(word = word, onClick = { onWordClick(word.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCallout(dueCount: Int, wordCount: Int, onStartReview: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (dueCount > 0) {
                    Text(
                        stringResource(R.string.feature_home_due_title, dueCount.toPersianDigits()),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(stringResource(R.string.feature_home_due_body), style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        stringResource(R.string.feature_home_dictionary_size, wordCount.toPersianDigits()),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            if (dueCount > 0) {
                Button(onClick = onStartReview) { Text(stringResource(R.string.feature_home_start_review)) }
            }
        }
    }
}

@Composable
private fun TipCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(RavanIcons.Tip, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.feature_home_tip_title), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.feature_home_tip_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@DevicePreviews
@Composable
private fun HomeContentPreview() {
    val word = Word(
        id = "1",
        text = "serendipity",
        phoneticUs = "/ˌserənˈdɪpəti/",
        meanings = listOf(Meaning("کشف خوشایند و اتفاقی")),
    )
    RavanTheme {
        HomeContent(
            query = "",
            onQueryChange = {},
            searchState = SearchUiState.Idle,
            feedState = HomeFeedUiState.Success(word, listOf(word.copy(id = "2", text = "resilient")), 12, 71, Accent.US),
            speakingText = null,
            onSpeak = {},
            onWordClick = {},
            onAddWord = {},
            onStartReview = {},
        )
    }
}
