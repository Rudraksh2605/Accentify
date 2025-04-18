package com.developerspoint.accentify.ChatBot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developerspoint.accentify.Model.ChatMessage
import com.developerspoint.accentify.R
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val recyclerView: RecyclerView) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private val messages = mutableListOf<ChatMessage>()

    // Add message to the list and update the RecyclerView
    fun addMessage(text: String, isUser: Boolean) {
        val message = ChatMessage(text, isUser, System.currentTimeMillis()) // Add timestamp
        messages.add(message)
        notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    // Scroll to the bottom of the RecyclerView
    private fun scrollToBottom() {
        recyclerView.post {
            (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                messages.size - 1,
                0
            )
        }
    }

    // Determine the view type based on the sender (user or bot)
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    // Inflate appropriate layout based on the view type (user or bot message)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_USER) {
            R.layout.item_chat_message_user
        } else {
            R.layout.item_chat_message_bot
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    // Bind the message data to the view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    // Get the total count of messages
    override fun getItemCount() = messages.size

    // ViewHolder for individual chat messages
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)

        // Bind the data of a chat message to the view
        fun bind(message: ChatMessage) {
            messageText.text = message.text

            // Formatting the timestamp
            val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
            timeText.text = formattedTime

            // Set the background color based on the sender
            val backgroundResource = if (message.isUser) {
                R.drawable.user_message_bg // Use a drawable resource for the user message background
            } else {
                R.drawable.bot_message_bg // Use a drawable resource for the bot message background
            }
            messageContainer.setBackgroundResource(backgroundResource)
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_BOT = 1
    }
}
