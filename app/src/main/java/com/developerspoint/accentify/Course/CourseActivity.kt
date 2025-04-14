package com.developerspoint.accentify.Course

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developerspoint.accentify.Model.Course
import com.developerspoint.accentify.NavBar.NavBar
import com.developerspoint.accentify.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CourseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter
    private var courseList = mutableListOf<Course>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        val navView = findViewById<View>(R.id.bottom_nav)
        NavBar(navView, this)


        setupRecyclerView()
        fetchCourses()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler)
        adapter = CourseAdapter(courseList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun fetchCourses() {
        val db = FirebaseFirestore.getInstance()
        db.collection("courses")
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val updatedList = mutableListOf<Course>()
                for (document in result) {
                    val course = document.toObject(Course::class.java)
                    updatedList.add(course)
                }
                adapter.updateList(updatedList)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching courses", exception)
            }
    }
}
