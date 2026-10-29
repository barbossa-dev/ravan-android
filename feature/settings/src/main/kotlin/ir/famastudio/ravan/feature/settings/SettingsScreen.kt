package ir.famastudio.ravan.feature.settings

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.famastudio.ravan.core.common.text.toPersianDigits
import ir.famastudio.ravan.core.designsystem.component.SectionHeader
import ir.famastudio.ravan.core.designsystem.icon.RavanIcons
import ir.famastudio.ravan.core.model.Accent
import ir.famastudio.ravan.core.model.BackupSummary
import ir.famastudio.ravan.core.model.ReviewDirection
import ir.famastudio.ravan.core.model.ThemeConfig
import ir.famastudio.ravan.core.model.UserData
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backupStatus by viewModel.backupStatus.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingImport by rememberSaveable { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(viewModel::export)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pendingImport = uri
    }

    val exportDone = stringResource(R.string.feature_settings_export_done)
    val importDone = stringResource(R.string.feature_settings_import_done)
    val failed = stringResource(R.string.feature_settings_backup_failed)
    LaunchedEffect(backupStatus) {
        val message = when (val status = backupStatus) {
            is BackupStatus.Exported -> exportDone.withSummary(status.summary)
            is BackupStatus.Imported -> importDone.withSummary(status.summary)
            BackupStatus.Failed -> failed
            BackupStatus.Working, null -> null
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onBackupStatusShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.feature_settings_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (val state = uiState) {
            SettingsUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is SettingsUiState.Success -> SettingsContent(
                userData = state.userData,
                wordCount = state.wordCount,
                isBackupWorking = backupStatus == BackupStatus.Working,
                contentPadding = padding,
                onThemeChange = viewModel::setTheme,
                onDynamicColorChange = viewModel::setDynamicColor,
                onAccentChange = viewModel::setAccent,
                onSpeechRateChange = viewModel::setSpeechRate,
                onAutoPronounceChange = viewModel::setAutoPronounce,
                onTestVoice = viewModel::testVoice,
                onDirectionChange = viewModel::setReviewDirection,
                onDailyGoalChange = viewModel::setDailyGoal,
                onExport = { exportLauncher.launch("ravan-backup-${LocalDate.now()}.json") },
                onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
            )
        }
    }

    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            icon = { Icon(RavanIcons.Import, contentDescription = null) },
            title = { Text(stringResource(R.string.feature_settings_import_confirm_title)) },
            text = { Text(stringResource(R.string.feature_settings_import_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingImport = null
                    viewModel.import(uri)
                }) { Text(stringResource(R.string.feature_settings_import_action)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) { Text(stringResource(R.string.feature_settings_cancel)) }
            },
        )
    }
}

private fun String.withSummary(summary: BackupSummary): String =
    format(summary.words.toPersianDigits(), summary.cards.toPersianDigits(), summary.decks.toPersianDigits())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    userData: UserData,
    wordCount: Int,
    isBackupWorking: Boolean,
    contentPadding: PaddingValues,
    onThemeChange: (ThemeConfig) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onAccentChange: (Accent) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onAutoPronounceChange: (Boolean) -> Unit,
    onTestVoice: (String) -> Unit,
    onDirectionChange: (ReviewDirection) -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    val testSentence = stringResource(R.string.feature_settings_test_sentence)
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "appearance") {
            SettingsSection(stringResource(R.string.feature_settings_appearance)) {
                LabeledSegments(
                    label = stringResource(R.string.feature_settings_theme),
                    options = listOf(
                        ThemeConfig.FOLLOW_SYSTEM to stringResource(R.string.feature_settings_theme_system),
                        ThemeConfig.LIGHT to stringResource(R.string.feature_settings_theme_light),
                        ThemeConfig.DARK to stringResource(R.string.feature_settings_theme_dark),
                    ),
                    selected = userData.themeConfig,
                    onSelect = onThemeChange,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SwitchRow(
                        icon = RavanIcons.Palette,
                        title = stringResource(R.string.feature_settings_dynamic_color),
                        body = stringResource(R.string.feature_settings_dynamic_color_body),
                        checked = userData.useDynamicColor,
                        onCheckedChange = onDynamicColorChange,
                    )
                }
            }
        }

        item(key = "pronunciation") {
            SettingsSection(stringResource(R.string.feature_settings_pronunciation)) {
                LabeledSegments(
                    label = stringResource(R.string.feature_settings_accent),
                    options = listOf(
                        Accent.US to stringResource(R.string.feature_settings_accent_us),
                        Accent.UK to stringResource(R.string.feature_settings_accent_uk),
                    ),
                    selected = userData.defaultAccent,
                    onSelect = onAccentChange,
                )
                var rate by remember(userData.speechRate) { mutableFloatStateOf(userData.speechRate) }
                Text(
                    stringResource(R.string.feature_settings_speech_rate, "×" + "%.1f".format(rate).toPersianDigits()),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Slider(
                    value = rate,
                    onValueChange = { rate = it },
                    onValueChangeFinished = { onSpeechRateChange(rate) },
                    valueRange = 0.5f..1.5f,
                    steps = 9,
                )
                SwitchRow(
                    icon = RavanIcons.Voice,
                    title = stringResource(R.string.feature_settings_auto_pronounce),
                    body = stringResource(R.string.feature_settings_auto_pronounce_body),
                    checked = userData.autoPronounce,
                    onCheckedChange = onAutoPronounceChange,
                )
                TextButton(onClick = { onTestVoice(testSentence) }) {
                    Icon(RavanIcons.Speaker, contentDescription = null)
                    Text(stringResource(R.string.feature_settings_test_voice), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        item(key = "review") {
            SettingsSection(stringResource(R.string.feature_settings_review)) {
                LabeledSegments(
                    label = stringResource(R.string.feature_settings_direction),
                    options = listOf(
                        ReviewDirection.ENGLISH_TO_PERSIAN to stringResource(R.string.feature_settings_direction_en_fa),
                        ReviewDirection.PERSIAN_TO_ENGLISH to stringResource(R.string.feature_settings_direction_fa_en),
                        ReviewDirection.MIXED to stringResource(R.string.feature_settings_direction_mixed),
                    ),
                    selected = userData.reviewDirection,
                    onSelect = onDirectionChange,
                )
                var goal by remember(userData.dailyGoal) { mutableFloatStateOf(userData.dailyGoal.toFloat()) }
                Text(
                    stringResource(R.string.feature_settings_daily_goal, goal.roundToInt().toPersianDigits()),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Slider(
                    value = goal,
                    onValueChange = { goal = it },
                    onValueChangeFinished = { onDailyGoalChange(goal.roundToInt()) },
                    valueRange = 5f..50f,
                    steps = 8,
                )
            }
        }

        item(key = "backup") {
            SettingsSection(stringResource(R.string.feature_settings_backup)) {
                ActionRow(
                    icon = RavanIcons.Export,
                    title = stringResource(R.string.feature_settings_export),
                    body = stringResource(R.string.feature_settings_export_body),
                    enabled = !isBackupWorking,
                    onClick = onExport,
                )
                ActionRow(
                    icon = RavanIcons.Import,
                    title = stringResource(R.string.feature_settings_import),
                    body = stringResource(R.string.feature_settings_import_body),
                    enabled = !isBackupWorking,
                    onClick = onImport,
                )
                if (isBackupWorking) {
                    Text(stringResource(R.string.feature_settings_backup_working), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item(key = "about") {
            val context = LocalContext.current
            val version = remember {
                runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
            }
            SettingsSection(stringResource(R.string.feature_settings_about)) {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = { Icon(RavanIcons.Info, contentDescription = null) },
                    headlineContent = { Text(stringResource(R.string.feature_settings_version, version.toPersianDigits())) },
                    supportingContent = {
                        Column {
                            Text(stringResource(R.string.feature_settings_word_count, wordCount.toPersianDigits()))
                            Text(stringResource(R.string.feature_settings_about_body))
                        }
                    },
                )
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = { Icon(RavanIcons.Library, contentDescription = null) },
                    headlineContent = { Text(stringResource(R.string.feature_settings_data_sources)) },
                    supportingContent = {
                        Text(
                            stringResource(R.string.feature_settings_data_sources_body),
                            style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Ltr),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionHeader(title)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> LabeledSegments(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Text(label, style = MaterialTheme.typography.labelLarge)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, text) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) { Text(text, maxLines = 1) }
        }
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { Icon(icon, contentDescription = null) },
        headlineContent = { Text(title) },
        supportingContent = { Text(body) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
    )
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    body: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        headlineContent = { Text(title) },
        supportingContent = { Text(body) },
    )
}
