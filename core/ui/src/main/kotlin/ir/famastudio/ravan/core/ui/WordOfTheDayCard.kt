package ir.famastudio.ravan.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.famastudio.ravan.core.designsystem.component.EnglishText
import ir.famastudio.ravan.core.designsystem.component.Pill
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.designsystem.theme.EnglishTextStyles
import ir.famastudio.ravan.core.model.Accent
import ir.famastudio.ravan.core.model.Word

@Composable
fun WordOfTheDayCard(
    word: Word,
    accent: Accent,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(
                    text = stringResource(R.string.core_ui_word_of_the_day),
                    icon = RavanIcons.WordOfDay,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = word.partOfSpeech.persianLabel,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                )
                FilledIconButton(
                    onClick = onSpeak,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        if (isSpeaking) RavanIcons.Speaking else RavanIcons.Speaker,
                        contentDescription = stringResource(R.string.core_ui_speak),
                    )
                }
            }
            EnglishText(word.text, style = EnglishTextStyles.headwordMedium)
            if (word.phonetic(accent).isNotBlank()) {
                EnglishText(word.phonetic(accent), style = EnglishTextStyles.phonetic)
            }
            Text(word.primaryTranslation, style = MaterialTheme.typography.titleMedium)
            word.examples.firstOrNull()?.let { example ->
                EnglishText(
                    example.english,
                    style = EnglishTextStyles.sentence,
                    alignToStart = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
