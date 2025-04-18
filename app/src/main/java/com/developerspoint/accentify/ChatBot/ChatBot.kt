package com.developerspoint.accentify.ChatBot

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerspoint.accentify.Model.ChatRequest
import com.developerspoint.accentify.Model.ChatResponse
import com.developerspoint.accentify.R
import com.developerspoint.accentify.databinding.ActivityChatBotBinding
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
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var chatApiService: ChatApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRetrofit()
        setupViews()
    }

    private fun setupRetrofit() {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://accentify-bot.onrender.com/")
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

        playAudio(response.audio_url)
    }

    private fun playAudio(audioPath: String) {
        try {
            if (::mediaPlayer.isInitialized) {
                mediaPlayer.release()
            }

            val audioUrl = "https://your-service-name.onrender.com$audioPath" // TODO: Full audio URL
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@ChatBot, "Error playing audio", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Audio error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        super.onDestroy()
    }
}
