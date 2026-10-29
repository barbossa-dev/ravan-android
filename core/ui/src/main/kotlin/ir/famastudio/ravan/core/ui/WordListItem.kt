package ir.famastudio.ravan.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import ir.famastudio.ravan.core.designsystem.component.EnglishText
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.designsystem.theme.EnglishTextStyles
import ir.famastudio.ravan.core.model.Word

/** The standard row for a word in every list of the app. */
@Composable
fun WordListItem(
    word: Word,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
    containerColor: Color = Color.Transparent,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = containerColor),
        leadingContent = {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    EnglishText(
                        text = word.text.take(1).uppercase(),
                        style = EnglishTextStyles.headwordSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        },
        headlineContent = {
            EnglishText(text = word.text, style = EnglishTextStyles.headwordSmall, maxLines = 1)
        },
        supportingContent = {
            Text(
                text = word.primaryTranslation,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            when {
                trailingContent != null -> trailingContent()
                word.isFavorite -> Icon(
                    RavanIcons.Favorite,
                    contentDescription = stringResource(R.string.core_ui_favorite),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
        },
    )
}
