package com.developerspoint.accentify.Result

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.developerspoint.accentify.R

class QuizResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_result)

        val resultTextView: TextView = findViewById(R.id.resultTextView)
        val scoreTextView: TextView = findViewById(R.id.scoreTextView)
        val xpEarnedTextView: TextView = findViewById(R.id.xpEarnedTextView)
        val xpProgress: ProgressBar = findViewById(R.id.xpProgress)
        val reviewButton: Button = findViewById(R.id.reviewButton)
        val finishButton: Button = findViewById(R.id.finishButton)

        val totalXp = intent.getIntExtra("totalXp", 0)
        val totalQuestions = intent.getIntExtra("totalQuestions", 0)

        resultTextView.text = "Quiz Completed!"
        scoreTextView.text = "$totalQuestions out of $totalQuestions correct"
        xpEarnedTextView.text = "+$totalXp XP Earned"

        val progress = (totalXp / (totalQuestions * 100.0) * 100).toInt()
        xpProgress.progress = progress

        finishButton.setOnClickListener {
            finish()
        }
    }
}
