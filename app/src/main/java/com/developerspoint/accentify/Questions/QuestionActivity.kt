package com.developerspoint.accentify.Questions

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.developerspoint.accentify.Model.Question
import com.developerspoint.accentify.R
import com.developerspoint.accentify.Result.QuizResultActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class QuestionActivity : AppCompatActivity() {

    private lateinit var questionTextView: TextView
    private lateinit var difficultyTextView: TextView
    private lateinit var xpTextView: TextView
    private lateinit var option1: RadioButton
    private lateinit var option2: RadioButton
    private lateinit var option3: RadioButton
    private lateinit var option4: RadioButton
    private lateinit var submitButton: Button
    private lateinit var explanationCard: CardView
    private lateinit var explanationTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var speakerButton: ImageButton

    private lateinit var optionCards: List<CardView>
    private lateinit var options: List<RadioButton>

    private val answerStatusList = mutableListOf<Boolean>() // true = correct, false = wrong


    private val db = FirebaseFirestore.getInstance()

    private var questionList = mutableListOf<Question>()
    private var currentQuestionIndex = 0
    private var selectedOptionIndex = -1
    private var isAnswerSubmitted = false

    private lateinit var courseId: String
    private lateinit var lessonId: String

    private lateinit var tts: TextToSpeech
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        // Receive courseId and lessonId from Intent
        courseId = intent.getStringExtra("courseId") ?: ""
        lessonId = intent.getStringExtra("lessonId") ?: ""

        if (courseId.isEmpty() || lessonId.isEmpty()) {
            Toast.makeText(this, "Invalid course or lesson", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        initTextToSpeech()
        loadAllQuestions()
    }

    private fun initViews() {
        questionTextView = findViewById(R.id.questionTextView)
        difficultyTextView = findViewById(R.id.difficultyTextView)
        xpTextView = findViewById(R.id.xpTextView)
        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)
        submitButton = findViewById(R.id.submitButton)
        explanationCard = findViewById(R.id.explanationCard)
        explanationTextView = findViewById(R.id.explanationTextView)
        progressBar = findViewById(R.id.progressBar)
        speakerButton = findViewById(R.id.speakerButton)

        optionCards = listOf(
            findViewById(R.id.optionCard0),
            findViewById(R.id.optionCard1),
            findViewById(R.id.optionCard2),
            findViewById(R.id.optionCard3)
        )
        options = listOf(option1, option2, option3, option4)

        explanationCard.visibility = View.GONE

        for (i in options.indices) {
            options[i].setOnClickListener {
                selectOption(i)
            }
        }

        submitButton.setOnClickListener {
            if (!isAnswerSubmitted) {
                if (selectedOptionIndex == -1) {
                    Toast.makeText(this, "Please select an option!", Toast.LENGTH_SHORT).show()
                } else {
                    showExplanation()
                }
            } else {
                goToNextQuestion()
            }
        }

        speakerButton.setOnClickListener {
            speakQuestionAndOptions()
        }
    }

    private fun initTextToSpeech() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US) // Adjust language as needed
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

                if (!isTtsReady) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
            }
        }

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                runOnUiThread {
                    speakerButton.setImageResource(R.drawable.ic_speaker)
                }
            }

            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    speakerButton.setImageResource(R.drawable.ic_speaker)
                }
            }

            override fun onError(utteranceId: String?) {
                runOnUiThread {
                    speakerButton.setImageResource(R.drawable.ic_speaker)
                }
            }
        })
    }

    private fun speakQuestionAndOptions() {
        if (!isTtsReady) {
            Toast.makeText(this, "Text-to-speech not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val currentQuestion = questionList.getOrNull(currentQuestionIndex) ?: return

        // Stop any ongoing speech
        tts.stop()

        // Build the text to speak
        val questionText = currentQuestion.questionText
        val optionsText = currentQuestion.options.joinToString(", ") { it }
        val fullText = "$questionText. Options are: $optionsText"

        // Set speech parameters
        tts.setSpeechRate(0.9f) // Slightly slower than normal
        tts.setPitch(1.1f) // Slightly higher pitch

        // Speak the text
        tts.speak(fullText, TextToSpeech.QUEUE_FLUSH, null, "question_utterance")
    }

    private fun selectOption(index: Int) {
        selectedOptionIndex = index
        for (i in optionCards.indices) {
            if (i == index) {
                optionCards[i].setBackgroundResource(R.drawable.option_background_selected)
            } else {
                optionCards[i].setBackgroundResource(R.drawable.option_background_unselected)
            }
        }
    }

    private fun loadAllQuestions() {
        progressBar.visibility = View.VISIBLE

        val courseId = intent.getStringExtra("courseId") ?: ""
        val lessonId = intent.getStringExtra("lessonId") ?: ""

        if (courseId.isEmpty() || lessonId.isEmpty()) {
            Toast.makeText(this, "Invalid course or lesson!", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        db.collection("courses")
            .document(courseId)
            .collection("lessons")
            .document(lessonId)
            .collection("questions")
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE

                questionList.clear()

                for (doc in documents) {
                    val question = Question(
                        id = doc.id,
                        questionText = doc.getString("questionText") ?: "",
                        options = (doc.get("options") as? List<String>) ?: emptyList(),
                        correctAnswer = (doc.getLong("correctAnswer") ?: 0L).toInt(),
                        explanation = doc.getString("explanation") ?: "",
                        difficulty = (doc.getLong("difficulty") ?: 1L).toInt(),
                        xpValue = (doc.getLong("xpValue") ?: 0L).toInt(),
                        order = (doc.getLong("order") ?: 0L).toInt()
                    )
                    questionList.add(question)
                }

                if (questionList.isNotEmpty()) {
                    currentQuestionIndex = 0
                    displayQuestion()
                } else {
                    Toast.makeText(this, "No questions found in this lesson!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading questions: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun displayQuestion() {
        val currentQuestion = questionList[currentQuestionIndex]

        questionTextView.text = currentQuestion.questionText
        if (currentQuestion.options.size >= 4) {
            option1.text = currentQuestion.options[0]
            option2.text = currentQuestion.options[1]
            option3.text = currentQuestion.options[2]
            option4.text = currentQuestion.options[3]
        }

        difficultyTextView.text = when (currentQuestion.difficulty) {
            0 -> "Easy"
            1 -> "Medium"
            2 -> "Hard"
            else -> "Unknown"
        }

        xpTextView.text = "${currentQuestion.xpValue} XP"

        explanationCard.visibility = View.GONE
        submitButton.text = "Submit Answer"
        isAnswerSubmitted = false
        selectedOptionIndex = -1

        for (card in optionCards) {
            card.setBackgroundResource(R.drawable.option_background_unselected)
        }
    }

    private fun showExplanation() {
        val currentQuestion = questionList[currentQuestionIndex]
        explanationTextView.text = currentQuestion.explanation
        explanationCard.visibility = View.VISIBLE
        submitButton.text = "Next Question"
        isAnswerSubmitted = true

        optionCards[currentQuestion.correctAnswer].setBackgroundResource(R.drawable.option_background_correct)

        val isCorrect = selectedOptionIndex == currentQuestion.correctAnswer
        answerStatusList.add(isCorrect)

        if (selectedOptionIndex != -1 && !isCorrect) {
            optionCards[selectedOptionIndex].setBackgroundResource(R.drawable.option_background_incorrect)
        }
    }


    private fun goToNextQuestion() {
        tts.stop()

        currentQuestionIndex++
        if (currentQuestionIndex < questionList.size) {
            displayQuestion()
        } else {
            val totalXp = questionList.sumOf { it.xpValue }
            val intent = Intent(this, QuizResultActivity::class.java)
            intent.putExtra("totalXp", totalXp)
            intent.putExtra("totalQuestions", questionList.size)
            intent.putExtra("answerStatusList", ArrayList(answerStatusList)) // Pass the list!
            startActivity(intent)
            finish()
        }
    }


    override fun onPause() {
        super.onPause()
        tts.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}