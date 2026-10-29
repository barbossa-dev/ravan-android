package ir.famastudio.ravan.feature.library.deck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.core.designsystem.component.EmptyState
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.designsystem.theme.deckColor
import ir.famastudio.ravan.core.ui.WordListItem
import ir.famastudio.ravan.feature.library.DeckDialog
import ir.famastudio.ravan.feature.library.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeckDetailScreen(
    onBack: () -> Unit,
    onWordClick: (String) -> Unit,
    onReviewDeck: (String) -> Unit,
    viewModel: DeckDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val pickerWords by viewModel.pickerWords.collectAsStateWithLifecycle()
    val addedToReview by viewModel.addedToReview.collectAsStateWithLifecycle()

    var showRename by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val addedMessage = stringResource(R.string.feature_library_added_to_review)

    LaunchedEffect(uiState) { if (uiState is DeckDetailUiState.Deleted) onBack() }
    LaunchedEffect(addedToReview) {
        if (addedToReview) {
            snackbarHostState.showSnackbar(addedMessage)
            viewModel.onMessageShown()
        }
    }

    val success = uiState as? DeckDetailUiState.Success

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(success?.deck?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(RavanIcons.ArrowBack, contentDescription = stringResource(R.string.feature_library_back))
                    }
                },
                actions = {
                    if (success != null) {
                        IconButton(onClick = { showRename = true }) {
                            Icon(RavanIcons.Edit, contentDescription = stringResource(R.string.feature_library_rename))
                        }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(RavanIcons.Delete, contentDescription = stringResource(R.string.feature_library_delete_deck))
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (success != null) {
                ExtendedFloatingActionButton(
                    onClick = { showPicker = true },
                    icon = { Icon(RavanIcons.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.feature_library_add_words)) },
                )
            }
        },
    ) { padding ->
        if (success == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 96.dp,
                ),
            ) {
                item(key = "header") {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = deckColor(success.deck.colorIndex).copy(alpha = 0.16f)),
                    ) {
                        Column(Modifier.padding(20.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                stringResource(R.string.feature_library_count, success.words.size.toPersianDigits()),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onReviewDeck(success.deck.id) }, enabled = success.words.isNotEmpty()) {
                                    Icon(RavanIcons.Play, contentDescription = null)
                                    Text(stringResource(R.string.feature_library_review_deck), modifier = Modifier.padding(start = 6.dp))
                                }
                                FilledTonalButton(onClick = viewModel::addAllToReview, enabled = success.words.isNotEmpty()) {
                                    Text(stringResource(R.string.feature_library_add_all_to_review))
                                }
                            }
                        }
                    }
                }
                if (success.words.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            icon = RavanIcons.Deck,
                            title = stringResource(R.string.feature_library_deck_empty_title),
                            message = stringResource(R.string.feature_library_deck_empty_body),
                        )
                    }
                }
                items(success.words, key = { it.id }) { word ->
                    WordListItem(
                        word = word,
                        onClick = { onWordClick(word.id) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.removeWord(word.id) }) {
                                Icon(RavanIcons.Close, contentDescription = stringResource(R.string.feature_library_remove_from_deck))
                            }
                        },
                    )
                }
            }
        }
    }

    if (success != null && showRename) {
        DeckDialog(
            title = stringResource(R.string.feature_library_deck_dialog_edit),
            initialName = success.deck.name,
            initialColor = success.deck.colorIndex,
            onConfirm = { name, color ->
                viewModel.rename(name, color)
                showRename = false
            },
            onDismiss = { showRename = false },
        )
    }
    if (success != null && showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.feature_library_delete_deck_title, success.deck.name)) },
            text = { Text(stringResource(R.string.feature_library_delete_deck_body)) },
            confirmButton = {
                TextButton(onClick = { showDelete = false; viewModel.delete() }) {
                    Text(stringResource(R.string.feature_library_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.feature_library_cancel)) } },
        )
    }
    if (success != null && showPicker) {
        WordPickerSheet(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            words = pickerWords,
            excludedIds = success.words.mapTo(HashSet()) { it.id },
            onConfirm = { ids ->
                viewModel.addWords(ids)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}
