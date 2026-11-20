package com.liyuqihxc.voxledger.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("response_format") val responseFormat: ResponseFormat = ResponseFormat(),
    val temperature: Double = 0.0,
    val stream: Boolean = false,
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ResponseFormat(
    val type: String = "json_object",
)

@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val choices: List<Choice> = emptyList(),
    val error: ApiError? = null,
)

@Serializable
data class Choice(
    val message: ResponseMessage? = null,
)

@Serializable
data class ResponseMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class ApiError(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null,
)
