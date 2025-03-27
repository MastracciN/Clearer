package com.example.clearer.models

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// OpenAIApiService is used to call the OpenAI api
interface OpenAiService {
    // @Headers("Authorization: ")
    @POST("v1/chat/completions")
    suspend fun createCompletion(@Body request: OpenAIRequest): Response<OpenAIResponse>
}