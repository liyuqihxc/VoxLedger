package com.liyuqihxc.voxledger.ui.record

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liyuqihxc.voxledger.logic.Money
import com.liyuqihxc.voxledger.logic.RelativeDateParser
import com.liyuqihxc.voxledger.model.Categories
import com.liyuqihxc.voxledger.model.TransactionDraft
import com.liyuqihxc.voxledger.model.TransactionType
import com.liyuqihxc.voxledger.ui.rememberAppContainer
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: RecordViewModel = viewModel(
        factory = RecordViewModel.factory(rememberAppContainer(), androidx.compose.ui.platform.LocalContext.current),
    ),
) {
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val partialText by viewModel.partialText.collectAsStateWithLifecycle()
    val isParsing by viewModel.isParsing.collectAsStateWithLifecycle()
    val drafts by viewModel.drafts.collectAsStateWithLifecycle()
    val warnings by viewModel.warnings.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val parseCount by viewModel.parseCount.collectAsStateWithLifecycle()
    val engineReady by viewModel.engineReady.collectAsStateWithLifecycle()
    val modelReady by viewModel.modelReady.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记一笔") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        bottomBar = {
            if (drafts.isNotEmpty()) {
                Button(
                    onClick = { viewModel.save(onSaved) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text("保存 ${drafts.count { it.isComplete }} 笔记录")
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!modelReady) {
                item {
                    DownloadModelCard(
                        progress = downloadProgress,
                        onDownload = viewModel::downloadModel,
                    )
                }
            }

            message?.let { msg ->
                item {
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (modelReady) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = viewModel::updateInputText,
                            modifier = Modifier.weight(1f),
                            label = { Text("说一句或输入，如：午饭花了35块") },
                            minLines = 2,
                            maxLines = 4,
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = { viewModel.toggleRecording() },
                            enabled = hasPermission && engineReady,
                        ) {
                            if (isRecording) {
                                Text("停止", fontSize = 13.sp)
                            } else {
                                Text("🎤", fontSize = 20.sp)
                            }
                        }
                    }
                    if (isRecording) {
                        Text(
                            "正在聆听… ${partialText.ifBlank { "" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (!engineReady) {
                        Text(
                            "离线识别引擎加载中…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            if (modelReady) {
                item {
                    Button(
                        onClick = viewModel::parse,
                        enabled = !isParsing && inputText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (isParsing) {
                            CircularProgressIndicator(Modifier.width(20.dp).height(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("解析中…")
                        } else {
                            Text("解析")
                        }
                    }
                }

                warnings.forEach { warning ->
                    item {
                        Text(
                            "⚠ $warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                itemsIndexed(drafts, key = { index, _ -> index }) { index, draft ->
                    DraftEditorCard(
                        draft = draft,
                        generation = parseCount,
                        onChange = { viewModel.updateDraft(index, it) },
                        onRemove = { viewModel.removeDraft(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadModelCard(
    progress: Float?,
    onDownload: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("离线识别模型未安装", style = MaterialTheme.typography.titleMedium)
            Text(
                "首次使用需下载 SenseVoice 模型（约 228MB），下载后保存在本机，之后可离线使用。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val p = progress
            if (p != null) {
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "下载中 ${(p * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                )
            } else {
                Button(onClick = onDownload, modifier = Modifier.fillMaxWidth()) {
                    Text("下载模型")
                }
            }
        }
    }
}

@Composable
private fun DraftEditorCard(
    draft: TransactionDraft,
    generation: Int,
    onChange: (TransactionDraft) -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeSelector(draft.type, onChange = { onChange(draft.copy(type = it)) })
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "移除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AmountField(
                    draft = draft,
                    generation = generation,
                    onChange = onChange,
                    modifier = Modifier.weight(1f),
                )
                CategoryDropdown(
                    type = draft.type,
                    selectedId = draft.category,
                    onSelect = { onChange(draft.copy(category = it)) },
                )
            }

            OutlinedTextField(
                value = draft.description,
                onValueChange = { onChange(draft.copy(description = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
                singleLine = true,
            )

            DateField(
                draft = draft,
                generation = generation,
                onChange = onChange,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("出差报销", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = draft.reimbursable,
                    onCheckedChange = { onChange(draft.copy(reimbursable = it)) },
                )
            }
            if (draft.reimbursable) {
                OutlinedTextField(
                    value = draft.tripName.orEmpty(),
                    onValueChange = { onChange(draft.copy(tripName = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("出差事由/名称（可选）") },
                    singleLine = true,
                )
            }
        }
    }
}

@Composable
private fun TypeSelector(type: TransactionType, onChange: (TransactionType) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TransactionType.entries.forEach { t ->
            FilterChip(
                selected = type == t,
                onClick = { onChange(t) },
                label = { Text(t.label) },
            )
        }
    }
}

@Composable
private fun AmountField(
    draft: TransactionDraft,
    generation: Int,
    onChange: (TransactionDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    var amountText by remember(generation) {
        mutableStateOf(draft.amountCents?.let { Money.formatCents(it) } ?: "")
    }
    OutlinedTextField(
        value = amountText,
        onValueChange = { text ->
            amountText = text
            onChange(draft.copy(amountCents = Money.parseToCents(text)))
        },
        modifier = modifier,
        label = { Text("金额") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = amountText.isNotBlank() && Money.parseToCents(amountText) == null,
        supportingText = if (amountText.isNotBlank() && Money.parseToCents(amountText) == null) {
            { Text("无法识别金额") }
        } else null,
    )
}

@Composable
private fun DateField(
    draft: TransactionDraft,
    generation: Int,
    onChange: (TransactionDraft) -> Unit,
) {
    var dateText by remember(generation) {
        mutableStateOf(draft.date?.toString() ?: "")
    }
    OutlinedTextField(
        value = dateText,
        onValueChange = { text ->
            dateText = text
            val parsed = RelativeDateParser.parse(text, LocalDate.now())
            onChange(draft.copy(date = parsed))
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("日期（如 昨天 / 11-18 / 2026-11-18）") },
        singleLine = true,
    )
}

@Composable
private fun CategoryDropdown(
    type: TransactionType,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    val categories = if (type == TransactionType.EXPENSE) Categories.EXPENSE else Categories.INCOME
    var expanded by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedId }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected?.let { "${it.emoji}${it.name}" } ?: "分类")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text("${category.emoji} ${category.name}") },
                    onClick = {
                        onSelect(category.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
