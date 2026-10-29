package ir.famastudio.ravan.feature.word

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.core.designsystem.component.EmptyState
import ir.famastudio.ravan.core.designsystem.component.EnglishText
import ir.famastudio.ravan.core.designsystem.component.LeitnerProgress
import ir.famastudio.ravan.core.designsystem.component.Pill
import ir.famastudio.ravan.core.designsystem.component.PronunciationButton
import ir.famastudio.ravan.core.designsystem.component.SectionHeader
import ir.famastudio.ravan.core.designsystem.component.SpeakIconButton
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.designsystem.theme.EnglishTextStyles
import ir.famastudio.ravan.core.designsystem.theme.deckColor
import ir.famastudio.ravan.core.domain.WordDetails
import ir.famastudio.ravan.core.model.Accent
import ir.famastudio.ravan.core.model.Leitner
import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.core.ui.boxLabel
import ir.famastudio.ravan.core.ui.dueLabel

@Composable
internal fun WordDetailScreen(
    onBack: () -> Unit,
    onWordClick: (String) -> Unit,
    onAddWord: (String) -> Unit,
    onEditWord: (String) -> Unit,
    viewModel: WordDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val speakingText by viewModel.speakingText.collectAsStateWithLifecycle()
    val isDeleted by viewModel.isDeleted.collectAsStateWithLifecycle()

    LaunchedEffect(isDeleted) { if (isDeleted) onBack() }

    WordDetailContent(
        uiState = uiState,
        speakingText = speakingText,
        onBack = onBack,
        onSpeak = viewModel::speak,
        onToggleFavorite = viewModel::toggleFavorite,
        onToggleReview = viewModel::toggleReview,
        onSetDecks = viewModel::setDecks,
        onCreateDeck = viewModel::createDeckWithWord,
        onDelete = viewModel::delete,
        onEdit = onEditWord,
        onRelatedClick = { text, wordId -> if (wordId != null) onWordClick(wordId) else onAddWord(text) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WordDetailContent(
    uiState: WordDetailUiState,
    speakingText: String?,
    onBack: () -> Unit,
    onSpeak: (String, Accent) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleReview: () -> Unit,
    onSetDecks: (Set<String>) -> Unit,
    onCreateDeck: (String) -> Unit,
    onDelete: () -> Unit,
    onEdit: (String) -> Unit,
    onRelatedClick: (text: String, wordId: String?) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showDeckSheet by rememberSaveable { mutableStateOf(false) }
    val success = uiState as? WordDetailUiState.Success

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    if (success != null && scrollBehavior.state.contentOffset < -120f) {
                        EnglishText(success.details.word.text, style = EnglishTextStyles.headwordSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(RavanIcons.ArrowBack, contentDescription = stringResource(R.string.feature_word_back))
                    }
                },
                actions = {
                    if (success != null) {
                        WordActions(
                            word = success.details.word,
                            onToggleFavorite = onToggleFavorite,
                            onEdit = { onEdit(success.details.word.id) },
                            onDelete = { showDeleteDialog = true },
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        when (uiState) {
            WordDetailUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            WordDetailUiState.NotFound -> EmptyState(
                icon = RavanIcons.Search,
                title = stringResource(R.string.feature_word_not_found),
                message = stringResource(R.string.feature_word_not_found_body),
                modifier = Modifier.padding(padding),
            )
            is WordDetailUiState.Success -> WordDetailBody(
                state = uiState,
                speakingText = speakingText,
                contentPadding = padding,
                onSpeak = onSpeak,
                onToggleReview = onToggleReview,
                onManageDecks = { showDeckSheet = true },
                onRelatedClick = onRelatedClick,
            )
        }
    }

    if (showDeleteDialog && success != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(RavanIcons.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.feature_word_delete_title, success.details.word.text)) },
            text = { Text(stringResource(R.string.feature_word_delete_body)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text(stringResource(R.string.feature_word_confirm_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.feature_word_cancel)) }
            },
        )
    }

    if (showDeckSheet && success != null) {
        DeckPickerSheet(
            decks = success.details.decks,
            selected = success.details.selectedDeckIds,
            onSelectionChange = onSetDecks,
            onCreateDeck = onCreateDeck,
            onDismiss = { showDeckSheet = false },
        )
    }
}

@Composable
private fun WordActions(
    word: Word,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.feature_word_share_chooser)
    var menuOpen by remember { mutableStateOf(false) }

    IconButton(onClick = onToggleFavorite) {
        Icon(
            imageVector = if (word.isFavorite) RavanIcons.Favorite else RavanIcons.FavoriteBorder,
            contentDescription = stringResource(
                if (word.isFavorite) R.string.feature_word_favorite_remove else R.string.feature_word_favorite_add,
            ),
            tint = if (word.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    IconButton(onClick = {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, word.shareText())
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }) {
        Icon(RavanIcons.Share, contentDescription = stringResource(R.string.feature_word_share))
    }
    Box {
        IconButton(onClick = { menuOpen = true }) {
            Icon(RavanIcons.MoreVert, contentDescription = stringResource(R.string.feature_word_more))
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feature_word_edit)) },
                leadingIcon = { Icon(RavanIcons.Edit, contentDescription = null) },
                onClick = { menuOpen = false; onEdit() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feature_word_delete), color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(RavanIcons.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = { menuOpen = false; onDelete() },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordDetailBody(
    state: WordDetailUiState.Success,
    speakingText: String?,
    contentPadding: PaddingValues,
    onSpeak: (String, Accent) -> Unit,
    onToggleReview: () -> Unit,
    onManageDecks: () -> Unit,
    onRelatedClick: (String, String?) -> Unit,
) {
    val details = state.details
    val word = details.word
    var lastAccent by remember { mutableStateOf(state.defaultAccent) }
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") { WordHeader(word) }

        item(key = "pronunciation") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PronunciationButton(
                    accentLabel = stringResource(R.string.feature_word_us),
                    phonetic = word.phoneticUs,
                    isSpeaking = speakingText == word.text && lastAccent == Accent.US,
                    onClick = {
                        lastAccent = Accent.US
                        onSpeak(word.text, Accent.US)
                    },
                    contentDescription = stringResource(R.string.feature_word_speak_us),
                    modifier = Modifier.weight(1f),
                )
                PronunciationButton(
                    accentLabel = stringResource(R.string.feature_word_uk),
                    phonetic = word.phoneticUk,
                    isSpeaking = speakingText == word.text && lastAccent == Accent.UK,
                    onClick = {
                        lastAccent = Accent.UK
                        onSpeak(word.text, Accent.UK)
                    },
                    contentDescription = stringResource(R.string.feature_word_speak_uk),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item(key = "leitner") { LeitnerCard(details = details, now = state.now, onToggleReview = onToggleReview) }

        if (word.meanings.isNotEmpty()) {
            item(key = "meanings-header") { SectionHeader(stringResource(R.string.feature_word_meanings)) }
            item(key = "meanings") {
                SectionCard {
                    word.meanings.forEachIndexed { index, meaning ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(28.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text((index + 1).toPersianDigits(), style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Text(meaning.translation, style = MaterialTheme.typography.titleMedium)
                                if (meaning.description.isNotBlank()) {
                                    Text(
                                        meaning.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (word.examples.isNotEmpty()) {
            item(key = "examples-header") { SectionHeader(stringResource(R.string.feature_word_examples)) }
            itemsIndexed(word.examples, key = { index, _ -> "example-$index" }) { _, example ->
                SectionCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            EnglishText(
                                example.english,
                                style = EnglishTextStyles.sentence,
                                alignToStart = false,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (example.persian.isNotBlank()) {
                                Text(
                                    example.persian,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        SpeakIconButton(
                            isSpeaking = speakingText == example.english,
                            onClick = { onSpeak(example.english, state.defaultAccent) },
                            contentDescription = stringResource(R.string.feature_word_speak_example),
                        )
                    }
                }
            }
        }

        if (word.synonyms.isNotEmpty()) {
            item(key = "synonyms") {
                RelatedWords(
                    title = stringResource(R.string.feature_word_synonyms),
                    words = word.synonyms,
                    relatedIds = details.relatedWordIds,
                    onClick = onRelatedClick,
                )
            }
        }
        if (word.antonyms.isNotEmpty()) {
            item(key = "antonyms") {
                RelatedWords(
                    title = stringResource(R.string.feature_word_antonyms),
                    words = word.antonyms,
                    relatedIds = details.relatedWordIds,
                    onClick = onRelatedClick,
                )
            }
        }

        if (word.note.isNotBlank()) {
            item(key = "note-header") { SectionHeader(stringResource(R.string.feature_word_note)) }
            item(key = "note") {
                SectionCard { Text(word.note, style = MaterialTheme.typography.bodyLarge) }
            }
        }

        item(key = "decks") {
            Column {
                SectionHeader(
                    stringResource(R.string.feature_word_decks),
                    action = { TextButton(onClick = onManageDecks) { Text(stringResource(R.string.feature_word_decks_manage)) } },
                )
                val selectedDecks = details.decks.filter { it.id in details.selectedDeckIds }
                if (selectedDecks.isEmpty()) {
                    Text(
                        stringResource(R.string.feature_word_decks_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedDecks.forEach { deck ->
                            Pill(
                                text = deck.name,
                                icon = RavanIcons.Deck,
                                containerColor = deckColor(deck.colorIndex).copy(alpha = 0.18f),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordHeader(word: Word) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EnglishText(word.text, style = EnglishTextStyles.headwordLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(word.partOfSpeech.persianLabel)
            word.level?.let { Pill(it.name) }
            if (word.isUserAdded) {
                Pill(
                    stringResource(R.string.feature_word_user_added),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun LeitnerCard(details: WordDetails, now: Long, onToggleReview: () -> Unit) {
    val card = details.card
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(RavanIcons.Review, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.feature_word_leitner), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (card != null) Pill(boxLabel(card))
            }
            if (card == null) {
                Text(
                    stringResource(R.string.feature_word_leitner_add_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onToggleReview, modifier = Modifier.fillMaxWidth()) {
                    Icon(RavanIcons.Add, contentDescription = null)
                    Text(stringResource(R.string.feature_word_leitner_add), modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                LeitnerProgress(box = card.box, boxCount = Leitner.BOX_COUNT, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(dueLabel(card, now), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            stringResource(
                                R.string.feature_word_leitner_stats,
                                card.reviewCount.toPersianDigits(),
                                card.correctCount.toPersianDigits(),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onToggleReview) { Text(stringResource(R.string.feature_word_leitner_remove)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelatedWords(
    title: String,
    words: List<String>,
    relatedIds: Map<String, String>,
    onClick: (String, String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            words.forEach { text ->
                val id = relatedIds[text]
                FilterChip(
                    selected = id != null,
                    onClick = { onClick(text, id) },
                    label = { EnglishText(text, style = MaterialTheme.typography.bodyLarge) },
                )
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

private fun Word.shareText(): String = buildString {
    append(text)
    if (phoneticUs.isNotBlank()) append("  ").append(phoneticUs)
    append('\n')
    meanings.forEach { append("• ").append(it.translation).append('\n') }
    examples.firstOrNull()?.let { append('\n').append(it.english).append('\n').append(it.persian) }
}
