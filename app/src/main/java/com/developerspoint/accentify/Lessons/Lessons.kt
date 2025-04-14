package com.developerspoint.accentify.Lessons

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developerspoint.accentify.Model.Course
import com.developerspoint.accentify.Model.Lesson // Make sure this import exists
import com.developerspoint.accentify.NavBar.NavBar
import com.developerspoint.accentify.R
import com.google.firebase.firestore.FirebaseFirestore

class Lessons : AppCompatActivity() {

    private lateinit var course_title: TextView
    private lateinit var course_diff: TextView
    private lateinit var course_desc: TextView
    private lateinit var total_lessons: TextView
    private lateinit var total_xp: TextView
    private lateinit var start_button: TextView
    private lateinit var lessonAdapter: LessonAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lessons)

        setupNav()
        setupView()
        setupRecyclerView()
        fetchFromFirestore()
    }

    private fun setupNav() {
        val navbar = findViewById<View>(R.id.bottom_nav)
        NavBar(navbar, this)
    }

    private fun setupView() {
        course_title = findViewById(R.id.courseTitle)
        course_diff = findViewById(R.id.courseDifficulty)
        course_desc = findViewById(R.id.courseDescription)
        total_lessons = findViewById(R.id.totalLessons)
        total_xp = findViewById(R.id.totalXp)
        start_button = findViewById(R.id.startButton)
        recyclerView = findViewById(R.id.lesson_recycle)

        val data = retrieveIntent()

        course_title.text = data.title
        course_diff.text = data.difficulty
        course_desc.text = data.description
        total_lessons.text = data.totalLessons.toString()
        total_xp.text = data.totalXp.toString()
    }

    private fun retrieveIntent(): Course {
        val title = intent.getStringExtra("courseTitle") ?: "N/A"
        val description = intent.getStringExtra("courseDescription") ?: "N/A"
        val id = intent.getStringExtra("courseId") ?: "N/A"
        val difficulty = intent.getStringExtra("courseDifficulty") ?: "N/A"
        val totalLessons = intent.getIntExtra("courseTotalLessons", 0)
        val totalXp = intent.getIntExtra("courseTotalXp", 0)

        return Course(id, title, description, "N.A.", "NA", difficulty, totalLessons, totalXp, false, 0)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        lessonAdapter = LessonAdapter(emptyList()) { /* no-op or handle clicks if needed */ }
        recyclerView.adapter = lessonAdapter
    }

    private fun setupLessonRecycler(lessons: List<Lesson>) {
        lessonAdapter = LessonAdapter(lessons) { lesson ->
            // Handle lesson click here
            // For example, start a new Activity or show a Toast
            // You can use: lesson.id, lesson.title, etc.
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

    private fun fetchFromFirestore() {
        val courseId = intent.getStringExtra("courseId") ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("courses")
            .document(courseId)
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
}