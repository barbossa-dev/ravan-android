package ir.famastudio.ravan.feature.library

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.designsystem.theme.DeckPalette
import ir.famastudio.ravan.core.designsystem.theme.deckColor

/** Create / rename dialog for a deck, with a colour picker. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DeckDialog(
    title: String,
    initialName: String,
    initialColor: Int,
    onConfirm: (name: String, colorIndex: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var color by rememberSaveable { mutableIntStateOf(initialColor) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.feature_library_deck_name)) },
                    singleLine = true,
                )
                Text(stringResource(R.string.feature_library_deck_color), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DeckPalette.indices.forEach { index ->
                        val selected = index == color
                        val description = stringResource(R.string.feature_library_deck_color_item, (index + 1).toPersianDigits())
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(deckColor(index))
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape,
                                )
                                .clickable { color = index }
                                .semantics {
                                    role = Role.RadioButton
                                    this.selected = selected
                                    contentDescription = description
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) Icon(RavanIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.surface)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim(), color) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.feature_library_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_library_cancel)) }
        },
    )
}
