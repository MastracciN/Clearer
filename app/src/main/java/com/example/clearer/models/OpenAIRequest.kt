package com.example.clearer.models

data class OpenAIRequest(
    val model: String,
    val messages: List<Message>,
//    val max_tokens: Int,
//    val temperature: Float = 0f
)

data class Message(
    val role: String,
    val content: String
)