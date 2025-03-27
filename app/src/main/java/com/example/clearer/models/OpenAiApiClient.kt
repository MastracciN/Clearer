package com.example.clearer.models

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenAiApiClient {

    private const val BASE_URL = "https://api.openai.com/"

    private val httpClient = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val openAiService : OpenAiService = retrofit.create(OpenAiService::class.java)
}