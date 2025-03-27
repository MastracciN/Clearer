package com.example.clearer.vms

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clearer.models.Message
import com.example.clearer.models.OpenAIRequest
import com.example.clearer.models.OpenAIResponse
import com.example.clearer.models.OpenAiApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CameraFragmentViewModel : ViewModel() {

    // Calls the OpenAI API and returns a response based on the "content" in Message
    suspend fun callOpenAIApi(content: String): String {

        val messageList = mutableListOf(
            Message(
                role = "user",
                content = "Fix formatting issues. Remove anything unrelated to the text.\n $content"
            )
        )

        val request = OpenAIRequest(
            "gpt-4",
            messageList,
        )

        var results = ""
        viewModelScope.launch {
            try {
                val response = OpenAiApiClient.openAiService.createCompletion(request)
                results = handleResponse(response)
            }catch (e: Exception) {
                Log.d("ABC","OpenAI Call Error")
            }
        }
        return results
    }

    // Used in the calling of the OpenAI API
    private suspend fun handleResponse(response: Response<OpenAIResponse>): String{
        var results = ""
        withContext(Dispatchers.Main){
            if (response.isSuccessful){
                response.body()?.let { completionResponse ->
                    results = completionResponse.choices.firstOrNull()?.message?.content!!
                    Log.d("ABC", "Handle Response:\n $results")
                }
            } else {
                Log.d("ABC", "Response unsuccessful")
            }
        }
        return results
    }
}