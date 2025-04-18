package com.developerspoint.accentify.Home

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.developerspoint.accentify.ChatBot.ChatBot
import com.developerspoint.accentify.R
import com.developerspoint.accentify.NavBar.NavBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class Home : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var userId: String? = null
    private lateinit var streakTxt: TextView
    private lateinit var chatbotFab: FloatingActionButton
    private lateinit var start_chat: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        db = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid

        streakTxt = findViewById(R.id.streak_txt)

        navBar()
        date_time()
        fetchAndUpdateUserStreak()
        chatbotFab = findViewById(R.id.chatbot_fab)
        start_chat = findViewById(R.id.ready_to_chat_btn)


        chatbotFab.setOnClickListener {
            navigateToChatBot()
        }
        start_chat.setOnClickListener {
            navigateToChatBot()
        }
    }

    private fun navBar() {
        val navView = findViewById<View>(R.id.bottom_nav)
        NavBar(navView, this)
    }

    private fun date_time() {
        val timeDateTxt = findViewById<TextView>(R.id.time_date_day)
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, MMMM d 'at' hh:mm a", Locale.getDefault())
        timeDateTxt.text = dateFormat.format(cal.time)
    }

    private fun fetchAndUpdateUserStreak() {
        userId?.let { id ->
            db.collection("Users").document(id)  // Corrected to "Users" (capital U)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val lastOpened = document.getString("lastOpenedDate") ?: ""
                        val currentStreak = document.getLong("currentStreak")?.toInt() ?: 0
                        val today = getTodayDate()

                        if (lastOpened.isEmpty() || lastOpened == "1") {
                            // No valid last opened date
                            updateUserStreak(currentStreak + 1, today)
                        } else {
                            val daysGap = calculateDaysBetween(lastOpened, today)

                            when {
                                daysGap == 0L -> {
                                    // Same day, no change
                                    streakTxt.text = currentStreak.toString()
                                }
                                daysGap == 1L -> {
                                    // Next consecutive day, increase streak
                                    updateUserStreak(currentStreak + 1, today)
                                }
                                else -> {
                                    // Missed day(s), reset streak
                                    updateUserStreak(1, today)
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "User document not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { error ->
                    error.printStackTrace()
                    Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserStreak(newStreak: Int, today: String) {
        userId?.let { id ->
            db.collection("Users").document(id)
                .update(
                    mapOf(
                        "currentStreak" to newStreak,
                        "lastOpenedDate" to today
                    )
                )
                .addOnSuccessListener {
                    streakTxt.text = newStreak.toString()
                }
                .addOnFailureListener { error ->
                    error.printStackTrace()
                    Toast.makeText(this, "Failed to update streak.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun calculateDaysBetween(startDate: String, endDate: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = format.parse(startDate)
            val end = format.parse(endDate)

            val diff = end.time - start.time
            TimeUnit.MILLISECONDS.toDays(diff)
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    private fun navigateToChatBot() {
        val intent = Intent(this, ChatBot::class.java)
        startActivity(intent)
    }
}
