package ir.famastudio.ravan.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.designsystem.theme.EnglishTextStyles

/** "US /rɪˈzɪliənt/" button. Shows an equaliser while that voice is speaking. */
@Composable
fun PronunciationButton(
    accentLabel: String,
    phonetic: String,
    isSpeaking: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnimatedContent(targetState = isSpeaking, label = "speaker") { speaking ->
                Icon(
                    imageVector = if (speaking) RavanIcons.Speaking else RavanIcons.Speaker,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column {
                Text(accentLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (phonetic.isNotBlank()) {
                    EnglishText(phonetic, style = EnglishTextStyles.phonetic)
                }
            }
        }
    }
}

/** Small round speaker button for example sentences and list rows. */
@Composable
fun SpeakIconButton(
    isSpeaking: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tonal: Boolean = false,
) {
    val icon = if (isSpeaking) RavanIcons.Speaking else RavanIcons.Speaker
    if (tonal) {
        FilledTonalIconButton(onClick = onClick, modifier = modifier) {
            Icon(icon, contentDescription = contentDescription)
        }
    } else {
        IconButton(onClick = onClick, modifier = modifier) {
            Icon(icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
