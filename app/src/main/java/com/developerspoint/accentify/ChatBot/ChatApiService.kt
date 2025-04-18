package com.developerspoint.accentify.ChatBot

import com.developerspoint.accentify.Model.ChatRequest
import com.developerspoint.accentify.Model.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {
    @POST("chat")
    fun sendMessage(@Body request: ChatRequest): Call<ChatResponse>
}