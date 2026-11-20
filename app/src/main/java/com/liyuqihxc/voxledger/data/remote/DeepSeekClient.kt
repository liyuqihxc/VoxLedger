package com.liyuqihxc.voxledger.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Thin wrapper around the DeepSeek chat-completions endpoint. Returns the raw
 * `choices[0].message.content` string (expected to be JSON), or a failure.
 */
class DeepSeekClient(
    private val client: OkHttpClient,
    private val baseUrl: String = "https://api.deepseek.com",
    private val json: Json = Json { ignoreUnknownKeys = true },
) {

    suspend fun chat(model: String, apiKey: String, system: String, user: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = ChatCompletionRequest(
                    model = model,
                    messages = listOf(
                        ChatMessage(role = "system", content = system),
                        ChatMessage(role = "user", content = user),
                    ),
                )
                val body = json.encodeToString(ChatCompletionRequest.serializer(), request)
                val httpRequest = Request.Builder()
                    .url("$baseUrl/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(httpRequest).execute().use { response ->
                    val bodyText = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val apiError = runCatching {
                            json.decodeFromString<ChatCompletionResponse>(bodyText).error?.message
                        }.getOrNull()
                        throw IOException(apiError ?: "HTTP ${response.code}")
                    }
                    val parsed = json.decodeFromString<ChatCompletionResponse>(bodyText)
                    parsed.choices.firstOrNull()?.message?.content
                        ?: throw IOException("模型未返回内容")
                }
            }
        }
}
