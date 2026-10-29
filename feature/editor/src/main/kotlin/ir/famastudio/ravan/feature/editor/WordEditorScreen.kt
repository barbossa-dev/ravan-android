package ir.famastudio.ravan.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.famastudio.ravan.core.designsystem.component.EnglishText
import ir.famastudio.ravan.core.designsystem.component.SectionHeader
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.model.CefrLevel
import ir.famastudio.ravan.core.model.PartOfSpeech

@Composable
internal fun WordEditorScreen(
    onClose: () -> Unit,
    onSaved: (String, Boolean) -> Unit,
    onOpenWord: (String) -> Unit,
    viewModel: WordEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedWordId) {
        state.savedWordId?.let { onSaved(it, state.isNew) }
    }

    WordEditorContent(
        state = state,
        actions = viewModel,
        onClose = onClose,
        onOpenWord = onOpenWord,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun WordEditorContent(
    state: WordEditorUiState,
    actions: WordEditorViewModel,
    onClose: () -> Unit,
    onOpenWord: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (state.isNew) R.string.feature_editor_new_title else R.string.feature_editor_edit_title))
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(RavanIcons.Close, contentDescription = stringResource(R.string.feature_editor_close))
                    }
                },
                actions = {
                    Button(
                        onClick = actions::save,
                        enabled = !state.isSaving && !state.isLoading,
                        modifier = Modifier.padding(end = 8.dp),
                    ) { Text(stringResource(R.string.feature_editor_save)) }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 48.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "word") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LtrTextField(
                            value = state.text,
                            onValueChange = actions::onTextChange,
                            label = stringResource(R.string.feature_editor_word),
                            isError = state.error == EditorError.EMPTY_WORD || state.error == EditorError.DUPLICATE,
                            textStyle = MaterialTheme.typography.titleLarge,
                        )
                        ErrorMessage(state = state, onOpenWord = onOpenWord)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LtrTextField(
                                value = state.phoneticUs,
                                onValueChange = actions::onPhoneticUsChange,
                                label = stringResource(R.string.feature_editor_phonetic_us),
                                placeholder = stringResource(R.string.feature_editor_phonetic_hint),
                                modifier = Modifier.weight(1f),
                            )
                            LtrTextField(
                                value = state.phoneticUk,
                                onValueChange = actions::onPhoneticUkChange,
                                label = stringResource(R.string.feature_editor_phonetic_uk),
                                placeholder = stringResource(R.string.feature_editor_phonetic_hint),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                item(key = "pos") {
                    Column {
                        SectionHeader(stringResource(R.string.feature_editor_part_of_speech))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PartOfSpeech.entries.forEach { pos ->
                                FilterChip(
                                    selected = state.partOfSpeech == pos,
                                    onClick = { actions.onPartOfSpeechChange(pos) },
                                    label = { Text(pos.persianLabel) },
                                )
                            }
                        }
                    }
                }

                item(key = "level") {
                    Column {
                        SectionHeader(stringResource(R.string.feature_editor_level))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.level == null,
                                onClick = { actions.onLevelChange(null) },
                                label = { Text(stringResource(R.string.feature_editor_level_unknown)) },
                            )
                            CefrLevel.entries.forEach { level ->
                                FilterChip(
                                    selected = state.level == level,
                                    onClick = { actions.onLevelChange(level) },
                                    label = { Text(level.name) },
                                )
                            }
                        }
                    }
                }

                item(key = "meanings-header") { SectionHeader(stringResource(R.string.feature_editor_meanings)) }
                itemsIndexed(state.meanings, key = { index, _ -> "meaning-$index" }) { index, meaning ->
                    EditorCard(onRemove = { actions.removeMeaning(index) }, canRemove = state.meanings.size > 1) {
                        OutlinedTextField(
                            value = meaning.translation,
                            onValueChange = { actions.onMeaningChange(index, meaning.copy(translation = it)) },
                            label = { Text(stringResource(R.string.feature_editor_meaning_translation)) },
                            isError = state.error == EditorError.NO_MEANING && index == 0,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        )
                        OutlinedTextField(
                            value = meaning.description,
                            onValueChange = { actions.onMeaningChange(index, meaning.copy(description = it)) },
                            label = { Text(stringResource(R.string.feature_editor_meaning_description)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                item(key = "add-meaning") {
                    TextButton(onClick = actions::addMeaning) {
                        Icon(RavanIcons.Add, contentDescription = null)
                        Text(stringResource(R.string.feature_editor_add_meaning), modifier = Modifier.padding(start = 6.dp))
                    }
                }

                item(key = "examples-header") { SectionHeader(stringResource(R.string.feature_editor_examples)) }
                itemsIndexed(state.examples, key = { index, _ -> "example-$index" }) { index, example ->
                    EditorCard(onRemove = { actions.removeExample(index) }, canRemove = state.examples.size > 1) {
                        LtrTextField(
                            value = example.english,
                            onValueChange = { actions.onExampleChange(index, example.copy(english = it)) },
                            label = stringResource(R.string.feature_editor_example_english),
                            singleLine = false,
                            capitalize = true,
                        )
                        OutlinedTextField(
                            value = example.persian,
                            onValueChange = { actions.onExampleChange(index, example.copy(persian = it)) },
                            label = { Text(stringResource(R.string.feature_editor_example_persian)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                item(key = "add-example") {
                    TextButton(onClick = actions::addExample) {
                        Icon(RavanIcons.Add, contentDescription = null)
                        Text(stringResource(R.string.feature_editor_add_example), modifier = Modifier.padding(start = 6.dp))
                    }
                }

                item(key = "synonyms") {
                    RelatedInput(
                        title = stringResource(R.string.feature_editor_synonyms),
                        values = state.synonyms,
                        onAdd = actions::addSynonym,
                        onRemove = actions::removeSynonym,
                    )
                }
                item(key = "antonyms") {
                    RelatedInput(
                        title = stringResource(R.string.feature_editor_antonyms),
                        values = state.antonyms,
                        onAdd = actions::addAntonym,
                        onRemove = actions::removeAntonym,
                    )
                }

                item(key = "note") {
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = actions::onNoteChange,
                        label = { Text(stringResource(R.string.feature_editor_note)) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (state.isNew) {
                    item(key = "review") {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = { Icon(RavanIcons.Review, contentDescription = null) },
                                headlineContent = { Text(stringResource(R.string.feature_editor_add_to_review)) },
                                supportingContent = { Text(stringResource(R.string.feature_editor_add_to_review_body)) },
                                trailingContent = {
                                    Switch(checked = state.addToReview, onCheckedChange = actions::onAddToReviewChange)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(state: WordEditorUiState, onOpenWord: (String) -> Unit) {
    val message = when (state.error) {
        EditorError.EMPTY_WORD -> R.string.feature_editor_error_empty_word
        EditorError.NO_MEANING -> R.string.feature_editor_error_no_meaning
        EditorError.DUPLICATE -> R.string.feature_editor_error_duplicate
        null -> return
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(message),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        val duplicateId = state.duplicateWordId
        if (duplicateId != null) {
            AssistChip(onClick = { onOpenWord(duplicateId) }, label = { Text(stringResource(R.string.feature_editor_open_existing)) })
        }
    }
}

@Composable
private fun EditorCard(
    onRemove: () -> Unit,
    canRemove: Boolean,
    content: @Composable () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(RavanIcons.Delete, contentDescription = stringResource(R.string.feature_editor_remove))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelatedInput(
    title: String,
    values: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val submit = {
        onAdd(input)
        input = ""
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title)
        Row(verticalAlignment = Alignment.CenterVertically) {
            LtrTextField(
                value = input,
                onValueChange = { input = it },
                label = stringResource(R.string.feature_editor_add_related_hint),
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Done,
                onImeAction = submit,
            )
            IconButton(onClick = submit, enabled = input.isNotBlank()) {
                Icon(RavanIcons.Add, contentDescription = stringResource(R.string.feature_editor_add))
            }
        }
        if (values.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                values.forEach { value ->
                    InputChip(
                        selected = false,
                        onClick = { onRemove(value) },
                        label = { EnglishText(value, style = MaterialTheme.typography.bodyMedium) },
                        trailingIcon = {
                            Icon(
                                RavanIcons.Close,
                                contentDescription = stringResource(R.string.feature_editor_remove),
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}

/** A text field for English input: left-to-right, no auto-capitalisation for headwords. */
@Composable
private fun LtrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    placeholder: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    capitalize: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (placeholder != null) Text(placeholder) },
        isError = isError,
        singleLine = singleLine,
        modifier = modifier,
        textStyle = textStyle.copy(textDirection = TextDirection.Ltr),
        keyboardOptions = KeyboardOptions(
            capitalization = if (capitalize) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
            imeAction = imeAction,
        ),
        keyboardActions = if (onImeAction != null) KeyboardActions(onDone = { onImeAction() }) else KeyboardActions.Default,
    )
}
