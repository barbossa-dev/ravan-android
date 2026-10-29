package ir.famastudio.ravan.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.core.designsystem.component.EmptyState
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.designsystem.theme.deckColor
import ir.famastudio.ravan.core.model.Deck
import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.core.model.WordFilter
import ir.famastudio.ravan.core.ui.WordListItem

@Composable
internal fun LibraryScreen(
    onWordClick: (String) -> Unit,
    onAddWord: () -> Unit,
    onDeckClick: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val tab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val words by viewModel.words.collectAsStateWithLifecycle()
    val decks by viewModel.decks.collectAsStateWithLifecycle()

    LibraryContent(
        tab = tab,
        filter = filter,
        words = words,
        decks = decks,
        onTabSelect = viewModel::selectTab,
        onFilterChange = viewModel::setFilter,
        onWordClick = onWordClick,
        onAddWord = onAddWord,
        onDeckClick = onDeckClick,
        onCreateDeck = viewModel::createDeck,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryContent(
    tab: LibraryTab,
    filter: WordFilter,
    words: List<Word>?,
    decks: List<Deck>?,
    onTabSelect: (LibraryTab) -> Unit,
    onFilterChange: (WordFilter) -> Unit,
    onWordClick: (String) -> Unit,
    onAddWord: () -> Unit,
    onDeckClick: (String) -> Unit,
    onCreateDeck: (String, Int) -> Unit,
) {
    var showDeckDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text(stringResource(R.string.feature_library_title)) })
                PrimaryTabRow(selectedTabIndex = tab.ordinal) {
                    Tab(
                        selected = tab == LibraryTab.WORDS,
                        onClick = { onTabSelect(LibraryTab.WORDS) },
                        text = { Text(stringResource(R.string.feature_library_tab_words)) },
                    )
                    Tab(
                        selected = tab == LibraryTab.DECKS,
                        onClick = { onTabSelect(LibraryTab.DECKS) },
                        text = { Text(stringResource(R.string.feature_library_tab_decks)) },
                    )
                }
            }
        },
        floatingActionButton = {
            when (tab) {
                LibraryTab.WORDS -> ExtendedFloatingActionButton(
                    onClick = onAddWord,
                    icon = { Icon(RavanIcons.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.feature_library_new_word)) },
                )
                LibraryTab.DECKS -> ExtendedFloatingActionButton(
                    onClick = { showDeckDialog = true },
                    icon = { Icon(RavanIcons.Deck, contentDescription = null) },
                    text = { Text(stringResource(R.string.feature_library_new_deck)) },
                )
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (tab) {
                LibraryTab.WORDS -> WordsTab(words = words, filter = filter, onFilterChange = onFilterChange, onWordClick = onWordClick)
                LibraryTab.DECKS -> DecksTab(decks = decks, onDeckClick = onDeckClick, onCreateDeck = { showDeckDialog = true })
            }
        }
    }

    if (showDeckDialog) {
        DeckDialog(
            title = stringResource(R.string.feature_library_deck_dialog_create),
            initialName = "",
            initialColor = (decks?.size ?: 0) % 8,
            onConfirm = { name, color ->
                onCreateDeck(name, color)
                showDeckDialog = false
            },
            onDismiss = { showDeckDialog = false },
        )
    }
}

@Composable
private fun WordsTab(
    words: List<Word>?,
    filter: WordFilter,
    onFilterChange: (WordFilter) -> Unit,
    onWordClick: (String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WordFilter.entries.forEach { option ->
                FilterChip(
                    selected = option == filter,
                    onClick = { onFilterChange(option) },
                    label = { Text(stringResource(option.labelRes())) },
                )
            }
        }
        when {
            words == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            words.isEmpty() -> EmptyState(
                icon = RavanIcons.Library,
                title = stringResource(R.string.feature_library_empty_title),
                message = stringResource(filter.emptyMessageRes()),
            )
            else -> LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                item {
                    Text(
                        stringResource(R.string.feature_library_count, words.size.toPersianDigits()),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
                items(words, key = { it.id }) { word ->
                    WordListItem(word = word, onClick = { onWordClick(word.id) })
                }
            }
        }
    }
}

@Composable
private fun DecksTab(
    decks: List<Deck>?,
    onDeckClick: (String) -> Unit,
    onCreateDeck: () -> Unit,
) {
    when {
        decks == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        decks.isEmpty() -> EmptyState(
            icon = RavanIcons.Deck,
            title = stringResource(R.string.feature_library_empty_decks_title),
            message = stringResource(R.string.feature_library_empty_decks_body),
            actionLabel = stringResource(R.string.feature_library_new_deck),
            onAction = onCreateDeck,
        )
        else -> LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(decks, key = { it.id }) { deck -> DeckCard(deck = deck, onClick = { onDeckClick(deck.id) }) }
        }
    }
}

@Composable
private fun DeckCard(deck: Deck, onClick: () -> Unit) {
    val color = deckColor(deck.colorIndex)
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.14f)),
    ) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color),
                contentAlignment = Alignment.Center,
            ) {
                Icon(RavanIcons.Deck, contentDescription = null, tint = MaterialTheme.colorScheme.surface)
            }
            Text(deck.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(
                stringResource(R.string.feature_library_count, deck.wordCount.toPersianDigits()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun WordFilter.labelRes(): Int = when (this) {
    WordFilter.RECENT -> R.string.feature_library_filter_recent
    WordFilter.FAVORITES -> R.string.feature_library_filter_favorites
    WordFilter.MINE -> R.string.feature_library_filter_mine
    WordFilter.IN_REVIEW -> R.string.feature_library_filter_review
}

private fun WordFilter.emptyMessageRes(): Int = when (this) {
    WordFilter.RECENT -> R.string.feature_library_empty_recent
    WordFilter.MINE -> R.string.feature_library_empty_mine
    WordFilter.FAVORITES -> R.string.feature_library_empty_favorites
    WordFilter.IN_REVIEW -> R.string.feature_library_empty_review
}
