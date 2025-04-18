package com.developerspoint.accentify.Result

import android.os.Bundle
import android.util.Log
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
        val finishButton: Button = findViewById(R.id.finishButton)
        val correctAnswersTextView: TextView = findViewById(R.id.correctAnswersTextView)

        val totalQuestions = intent.getIntExtra("totalQuestions", 0)
        val answerStatusList = intent.getSerializableExtra("answerStatusList") as? ArrayList<Boolean> ?: arrayListOf()

        Log.d("QuizResultActivity", "Received answerStatusList: $answerStatusList")

        val correctAnswers = answerStatusList.count { it }

        val xpPerQuestion = 20
        val totalCorrectXp = correctAnswers * xpPerQuestion

        resultTextView.text = "Quiz Completed!"
        scoreTextView.text = "$correctAnswers out of $totalQuestions correct"
        correctAnswersTextView.text = "$correctAnswers correct answers"  // Update this dynamically
        xpEarnedTextView.text = "+$totalCorrectXp XP Earned"

        val progress = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
        xpProgress.progress = progress

        finishButton.setOnClickListener {
            finish()
        }
    }
}
