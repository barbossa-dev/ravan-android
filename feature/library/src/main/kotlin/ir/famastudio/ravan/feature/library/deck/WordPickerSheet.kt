package ir.famastudio.ravan.feature.library.deck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.core.designsystem.component.RavanSearchField
import ir.famastudio.ravan.core.model.Word
import ir.famastudio.ravan.core.ui.WordListItem
import ir.famastudio.ravan.feature.library.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WordPickerSheet(
    query: String,
    onQueryChange: (String) -> Unit,
    words: List<Word>,
    excludedIds: Set<String>,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val selected = remember { mutableStateListOf<String>() }
    val candidates = words.filterNot { it.id in excludedIds }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .fillMaxHeight(0.9f)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.feature_library_picker_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            RavanSearchField(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = stringResource(R.string.feature_library_picker_search),
                clearContentDescription = stringResource(R.string.feature_library_picker_clear),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyColumn(Modifier.weight(1f)) {
                items(candidates, key = { it.id }) { word ->
                    val checked = word.id in selected
                    val toggle = { if (checked) selected.remove(word.id) else selected.add(word.id); Unit }
                    WordListItem(
                        word = word,
                        onClick = toggle,
                        trailingContent = { Checkbox(checked = checked, onCheckedChange = { toggle() }) },
                    )
                }
            }
            Button(
                onClick = { onConfirm(selected.toSet()) },
                enabled = selected.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(stringResource(R.string.feature_library_picker_confirm, selected.size.toPersianDigits()))
            }
        }
    }
}
