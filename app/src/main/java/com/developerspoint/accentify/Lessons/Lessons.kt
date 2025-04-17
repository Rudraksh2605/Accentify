package com.developerspoint.accentify.Lessons

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developerspoint.accentify.Model.Course
import com.developerspoint.accentify.Model.Lesson
import com.developerspoint.accentify.NavBar.NavBar
import com.developerspoint.accentify.Questions.QuestionActivity
import com.developerspoint.accentify.R
import com.google.firebase.firestore.FirebaseFirestore

class Lessons : AppCompatActivity() {

    private lateinit var courseTitle: TextView
    private lateinit var courseDifficulty: TextView
    private lateinit var courseDescription: TextView
    private lateinit var totalLessons: TextView
    private lateinit var totalXp: TextView
    private lateinit var startButton: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var lessonAdapter: LessonAdapter

    private lateinit var courseData: Course // Save course data here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lessons)

        setupNav()
        setupViews()
        setupRecyclerView()
        fetchLessonsFromFirestore()
    }

    private fun setupNav() {
        val navbar = findViewById<View>(R.id.bottom_nav)
        NavBar(navbar, this)
    }

    private fun setupViews() {
        courseTitle = findViewById(R.id.courseTitle)
        courseDifficulty = findViewById(R.id.courseDifficulty)
        courseDescription = findViewById(R.id.courseDescription)
        totalLessons = findViewById(R.id.totalLessons)
        totalXp = findViewById(R.id.totalXp)
        startButton = findViewById(R.id.startButton)
        recyclerView = findViewById(R.id.lesson_recycle)

        courseData = retrieveIntentData()

        courseTitle.text = courseData.title
        courseDifficulty.text = courseData.difficulty
        courseDescription.text = courseData.description
        totalLessons.text = courseData.totalLessons.toString()
        totalXp.text = courseData.totalXp.toString()

        startButton.setOnClickListener {
            startFirstLesson()
        }
    }

    private fun retrieveIntentData(): Course {
        val title = intent.getStringExtra("courseTitle") ?: "N/A"
        val description = intent.getStringExtra("courseDescription") ?: "N/A"
        val id = intent.getStringExtra("courseId") ?: "N/A"
        val difficulty = intent.getStringExtra("courseDifficulty") ?: "N/A"
        val totalLessons = intent.getIntExtra("courseTotalLessons", 0)
        val totalXp = intent.getIntExtra("courseTotalXp", 0)

        return Course(id, title, description, "N/A", "N/A", difficulty, totalLessons, totalXp, false, 0)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        lessonAdapter = LessonAdapter(emptyList()) { /* No-op for now */ }
        recyclerView.adapter = lessonAdapter
    }

    private fun setupLessonRecycler(lessons: List<Lesson>) {
        lessonAdapter = LessonAdapter(lessons) { lesson ->
            openLesson(lesson)
        }
        recyclerView.adapter = lessonAdapter

        val divider = ContextCompat.getDrawable(this, R.drawable.divider)
        divider?.let {
            recyclerView.addItemDecoration(
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(it)
                }
            )
        }
    }

    private fun fetchLessonsFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("courses")
            .document(courseData.id)
            .collection("lessons")
            .orderBy("order")
            .get()
            .addOnSuccessListener { result ->
                val lessons = result.toObjects(Lesson::class.java)
                setupLessonRecycler(lessons)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting lessons: ", e)
            }
    }

    private fun startFirstLesson() {
        val db = FirebaseFirestore.getInstance()

        db.collection("courses")
            .document(courseData.id)
            .collection("lessons")
            .orderBy("order")
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val firstLesson = result.documents[0].toObject(Lesson::class.java)
                    firstLesson?.let { openLesson(it) }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error starting first lesson: ", e)
            }
    }

    private fun openLesson(lesson: Lesson) {
        val intent = Intent(this, QuestionActivity::class.java).apply {
            putExtra("courseId", courseData.id)
            putExtra("lessonId", lesson.id)
            Log.d("Lessons", "Lesson ID: ${lesson.id}")
            putExtra("lessonTitle", lesson.title)
            putExtra("lessonDescription", lesson.description)
            putExtra("lessonDuration", lesson.durationMinutes)
            putExtra("lessonXp", lesson.xpReward)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
