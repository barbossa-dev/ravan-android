package ir.famastudio.ravan.feature.word

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.famastudio.ravan.core.designsystem.theme.deckColor
import ir.famastudio.ravan.core.model.Deck

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeckPickerSheet(
    decks: List<Deck>,
    selected: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    onCreateDeck: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newDeckName by rememberSaveable { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.feature_word_decks_sheet_title), style = MaterialTheme.typography.titleLarge)
            decks.forEach { deck ->
                val checked = deck.id in selected
                val toggle = { onSelectionChange(if (checked) selected - deck.id else selected + deck.id) }
                ListItem(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(onClick = toggle),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = {
                        Box(
                            Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(deckColor(deck.colorIndex)),
                        )
                    },
                    headlineContent = { Text(deck.name) },
                    trailingContent = { Checkbox(checked = checked, onCheckedChange = { toggle() }) },
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newDeckName,
                    onValueChange = { newDeckName = it },
                    label = { Text(stringResource(R.string.feature_word_new_deck_hint)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalButton(
                    onClick = {
                        onCreateDeck(newDeckName.trim())
                        newDeckName = ""
                    },
                    enabled = newDeckName.isNotBlank(),
                ) { Text(stringResource(R.string.feature_word_create_deck)) }
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.feature_word_done)) }
        }
    }
}
