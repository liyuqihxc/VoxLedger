package com.liyuqihxc.voxledger.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liyuqihxc.voxledger.ui.rememberAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(rememberAppContainer())),
) {
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val model by viewModel.model.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val apiKeyInput by viewModel.apiKeyInput.collectAsStateWithLifecycle()
    val modelInput by viewModel.modelInput.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("DeepSeek 接入", style = MaterialTheme.typography.titleMedium)
            Text(
                "API Key 仅保存在本机，并用 Android Keystore 加密。记账文本会发送到 DeepSeek 云端处理。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = viewModel::updateApiKeyInput,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") },
                placeholder = { Text(if (apiKey != null) "已设置，输入新值可覆盖" else "sk-...") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::saveApiKey) { Text("保存") }
                OutlinedButton(onClick = viewModel::clearApiKey) { Text("清除") }
                if (apiKey != null) {
                    Text(
                        "已设置",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            OutlinedTextField(
                value = modelInput.ifEmpty { model },
                onValueChange = viewModel::updateModelInput,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("模型") },
                supportingText = { Text("默认 deepseek-chat，可改为 deepseek-reasoner 等") },
                singleLine = true,
            )
            Button(onClick = viewModel::saveModel) { Text("保存模型") }

            HorizontalDivider()

            Text("语音识别", style = MaterialTheme.typography.titleMedium)
            Text(
                "使用本地离线 SenseVoice 引擎识别，语音不出本机。首次使用时需在记账页下载模型（约 228MB）。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()

            Text("关于", style = MaterialTheme.typography.titleMedium)
            Text(
                "言账 VoxLedger v1.0.0\n自然语言语音记账 · 数据仅存本机",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            message?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
