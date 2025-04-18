package com.developerspoint.accentify.ChatBot

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerspoint.accentify.Model.ChatRequest
import com.developerspoint.accentify.Model.ChatResponse
import com.developerspoint.accentify.R
import com.developerspoint.accentify.databinding.ActivityChatBotBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ChatBot : AppCompatActivity() {

    private lateinit var binding: ActivityChatBotBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatApiService: ChatApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRetrofit()
        setupViews()

        val chatDocId = intent.getStringExtra("chatDocId")
        if (chatDocId != null) {
            loadOldChat(chatDocId)
            binding.messageEditText.visibility = View.GONE
            binding.sendButton.visibility = View.GONE
        }
    }

    private fun setupRetrofit() {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://accentify-bot.onrender.com/") // Change this to your bot's API URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        chatApiService = retrofit.create(ChatApiService::class.java)
    }

    private fun setupViews() {
        chatAdapter = ChatAdapter(binding.chatRecyclerView)
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatBot)
            adapter = chatAdapter
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                chatAdapter.addMessage(message, true)
                sendMessage(message)
                binding.messageEditText.text.clear()
            }
        }
    }

    private fun sendMessage(text: String) {
        binding.typingIndicator.visibility = View.VISIBLE
        binding.sendButton.isEnabled = false

        val request = ChatRequest(text)
        chatApiService.sendMessage(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                binding.typingIndicator.visibility = View.GONE
                binding.sendButton.isEnabled = true

                if (response.isSuccessful) {
                    response.body()?.let { chatResponse ->
                        handleChatResponse(chatResponse)
                        saveChatHistory(text, chatResponse.translation)
                    }
                } else {
                    Toast.makeText(
                        this@ChatBot,
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                binding.typingIndicator.visibility = View.GONE
                binding.sendButton.isEnabled = true
                Toast.makeText(
                    this@ChatBot,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun handleChatResponse(response: ChatResponse) {
        val botMessage = "${response.translation}\n\nExample: ${response.example}"
        chatAdapter.addMessage(botMessage, false)
    }

    private fun saveChatHistory(userMessage: String, botMessage: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val sessionTitle = "Session_${System.currentTimeMillis()}"

        val chatHistory = hashMapOf(
            "title" to sessionTitle,
            "userMessage" to userMessage,
            "botMessage" to botMessage,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("chatHistory")
            .add(chatHistory)

    }

    private fun loadOldChat(chatDocId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("chatHistory")
                .document(chatDocId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userMessage = document.getString("userMessage") ?: ""
                        val botMessage = document.getString("botMessage") ?: ""

                        chatAdapter.addMessage(userMessage, true)  // user's old message
                        chatAdapter.addMessage(botMessage, false) // bot's old reply
                    } else {
                        Toast.makeText(this, "Chat not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load chat", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroy() {
        chatAdapter.shutdown()
        super.onDestroy()
    }
}
