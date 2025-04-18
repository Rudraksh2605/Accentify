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

    fun addMessage(text: String, isUser: Boolean) {
        val message = ChatMessage(text, isUser, System.currentTimeMillis()) // Add timestamp
        messages.add(message)
        notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        recyclerView.post {
            (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                messages.size - 1,
                0
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_USER) {
            R.layout.item_chat_message_user
        } else {
            R.layout.item_chat_message_bot
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)

        fun bind(message: ChatMessage) {
            messageText.text = message.text

            val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
            timeText.text = formattedTime

            val backgroundResource = if (message.isUser) {
                R.drawable.user_message_bg
            } else {
                R.drawable.bot_message_bg
            }
            messageContainer.setBackgroundResource(backgroundResource)
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_BOT = 1
    }
}
