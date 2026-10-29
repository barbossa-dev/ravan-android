package ir.famastudio.ravan.feature.review.session

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.core.designsystem.component.EmptyState
import ir.famastudio.ravan.core.designsystem.component.EnglishText
import ir.famastudio.ravan.core.designsystem.component.Pill
import ir.famastudio.ravan.core.designsystem.component.SpeakIconButton
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.designsystem.theme.EnglishTextStyles
import ir.famastudio.ravan.core.model.ReviewDirection
import ir.famastudio.ravan.core.model.ReviewItem
import ir.famastudio.ravan.core.ui.WordListItem
import ir.famastudio.ravan.feature.review.R

@Composable
internal fun ReviewSessionScreen(
    onClose: () -> Unit,
    onWordClick: (String) -> Unit,
    viewModel: ReviewSessionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val speakingText by viewModel.speakingText.collectAsStateWithLifecycle()
    ReviewSessionContent(
        uiState = uiState,
        speakingText = speakingText,
        onClose = onClose,
        onFlip = viewModel::flip,
        onAnswer = viewModel::answer,
        onSpeak = viewModel::speak,
        onWordClick = onWordClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewSessionContent(
    uiState: ReviewSessionUiState,
    speakingText: String?,
    onClose: () -> Unit,
    onFlip: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    onSpeak: (String) -> Unit,
    onWordClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (uiState is ReviewSessionUiState.InProgress) {
                            Text(
                                stringResource(
                                    R.string.feature_review_progress,
                                    (uiState.index + 1).toPersianDigits(),
                                    uiState.items.size.toPersianDigits(),
                                ),
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(RavanIcons.Close, contentDescription = stringResource(R.string.feature_review_close))
                        }
                    },
                )
                if (uiState is ReviewSessionUiState.InProgress) {
                    val progress by animateFloatAsState(uiState.progress, label = "progress")
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState) {
                ReviewSessionUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                ReviewSessionUiState.Empty -> EmptyState(
                    icon = RavanIcons.Review,
                    title = stringResource(R.string.feature_review_session_empty_title),
                    message = stringResource(R.string.feature_review_session_empty_body),
                    actionLabel = stringResource(R.string.feature_review_finish),
                    onAction = onClose,
                )
                is ReviewSessionUiState.InProgress -> InProgress(uiState, speakingText, onFlip, onAnswer, onSpeak)
                is ReviewSessionUiState.Finished -> Finished(uiState, onClose, onWordClick)
            }
        }
    }
}

@Composable
private fun InProgress(
    state: ReviewSessionUiState.InProgress,
    speakingText: String?,
    onFlip: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    onSpeak: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.isPractice) {
            Pill(
                stringResource(R.string.feature_review_practice_badge),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        AnimatedContent(
            targetState = state.index,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                (slideInHorizontally(tween(300)) { -it / 3 } + fadeIn()) togetherWith fadeOut(tween(150))
            },
            label = "card",
        ) { index ->
            val item = state.items[index]
            FlipCard(
                item = item,
                isFlipped = state.isFlipped && index == state.index,
                speakingText = speakingText,
                onFlip = onFlip,
                onSpeak = onSpeak,
            )
        }
        if (!state.isFlipped) {
            Button(onClick = onFlip, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(stringResource(R.string.feature_review_show_answer))
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = { onAnswer(false) },
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Icon(RavanIcons.Close, contentDescription = null)
                    Text(stringResource(R.string.feature_review_did_not_know), modifier = Modifier.padding(start = 6.dp))
                }
                Button(
                    onClick = { onAnswer(true) },
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                ) {
                    Icon(RavanIcons.Check, contentDescription = null)
                    Text(stringResource(R.string.feature_review_knew), modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

/** A card that turns over in 3D: question on the front, full answer on the back. */
@Composable
private fun FlipCard(
    item: ReviewItem,
    isFlipped: Boolean,
    speakingText: String?,
    onFlip: () -> Unit,
    onSpeak: (String) -> Unit,
) {
    val rotation by animateFloatAsState(if (isFlipped) 180f else 0f, animationSpec = tween(450), label = "flip")
    val density = LocalDensity.current.density
    Card(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clickable(role = Role.Button, onClick = onFlip),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (rotation <= 90f) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationY = if (rotation > 90f) 180f else 0f }
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (rotation <= 90f) CardFront(item, speakingText, onSpeak) else CardBack(item, speakingText, onSpeak)
        }
    }
}

@Composable
private fun CardFront(item: ReviewItem, speakingText: String?, onSpeak: (String) -> Unit) {
    val word = item.word
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Pill(word.partOfSpeech.persianLabel)
        if (item.direction == ReviewDirection.PERSIAN_TO_ENGLISH) {
            word.meanings.take(2).forEach {
                Text(it.translation, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            }
        } else {
            EnglishText(word.text, style = EnglishTextStyles.headwordLarge)
            if (word.phoneticUs.isNotBlank()) EnglishText(word.phoneticUs, style = EnglishTextStyles.phonetic)
            SpeakIconButton(
                isSpeaking = speakingText == word.text,
                onClick = { onSpeak(word.text) },
                contentDescription = stringResource(R.string.feature_review_speak),
                tonal = true,
            )
        }
        Text(
            stringResource(R.string.feature_review_tap_to_flip),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}

@Composable
private fun CardBack(item: ReviewItem, speakingText: String?, onSpeak: (String) -> Unit) {
    val word = item.word
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                EnglishText(word.text, style = EnglishTextStyles.headwordMedium)
                if (word.phoneticUs.isNotBlank()) EnglishText(word.phoneticUs, style = EnglishTextStyles.phonetic)
            }
            SpeakIconButton(
                isSpeaking = speakingText == word.text,
                onClick = { onSpeak(word.text) },
                contentDescription = stringResource(R.string.feature_review_speak),
                tonal = true,
            )
        }
        HorizontalDivider()
        word.meanings.forEach { meaning ->
            Column {
                Text(meaning.translation, style = MaterialTheme.typography.titleLarge)
                if (meaning.description.isNotBlank()) {
                    Text(meaning.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        word.examples.firstOrNull()?.let { example ->
            HorizontalDivider()
            EnglishText(example.english, style = EnglishTextStyles.sentence, alignToStart = false, modifier = Modifier.fillMaxWidth())
            if (example.persian.isNotBlank()) {
                Text(example.persian, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Finished(
    state: ReviewSessionUiState.Finished,
    onClose: () -> Unit,
    onWordClick: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(RavanIcons.Trophy, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(96.dp))
        Text(stringResource(R.string.feature_review_finished_title), style = MaterialTheme.typography.headlineLarge)
        Text(
            stringResource(R.string.feature_review_finished_body, state.correct.toPersianDigits(), state.total.toPersianDigits()),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (state.toRetry.isNotEmpty()) {
            Text(
                stringResource(R.string.feature_review_finished_retry),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                state.toRetry.distinctBy { it.id }.forEach { word ->
                    WordListItem(word = word, onClick = { onWordClick(word.id) })
                }
            }
        }
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(top = 16.dp).heightIn(min = 56.dp)) {
            Text(stringResource(R.string.feature_review_finish))
        }
    }
}
