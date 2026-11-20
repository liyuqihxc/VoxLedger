package com.liyuqihxc.voxledger.data.remote

import com.liyuqihxc.voxledger.data.repository.SettingsRepository
import com.liyuqihxc.voxledger.model.ParseContext
import com.liyuqihxc.voxledger.model.ParseResult
import kotlinx.coroutines.flow.first

/**
 * DeepSeek-backed [TransactionParser]. Reads the API key and model name from
 * [SettingsRepository] at call time.
 */
class DeepSeekTransactionParser(
    private val client: DeepSeekClient,
    private val settings: SettingsRepository,
) : TransactionParser {

    override suspend fun parse(text: String, context: ParseContext): ParseResult {
        val apiKey = settings.apiKey.first()
            ?: return ParseResult(emptyList(), listOf("尚未配置 DeepSeek API Key，请在设置中填写"))
        val model = settings.model.first()

        val content = client.chat(
            model = model,
            apiKey = apiKey,
            system = PromptBuilder.buildSystem(context),
            user = text,
        ).getOrElse { error ->
            return ParseResult(emptyList(), listOf("调用 DeepSeek 失败: ${error.message ?: "未知错误"}"))
        }

        return DeepSeekResponseMapper.map(content, context)
    }
}
