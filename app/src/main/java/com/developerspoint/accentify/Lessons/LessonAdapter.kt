package com.developerspoint.accentify.Lessons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.developerspoint.accentify.Model.Lesson
import com.developerspoint.accentify.R

class LessonAdapter(
    private val lessons: List<Lesson>,
    private val onLessonClickListener: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    inner class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.lessonTitle)
        val duration: TextView = itemView.findViewById(R.id.lessonDuration)
        val xp: TextView = itemView.findViewById(R.id.lessonXp)
        val description: TextView = itemView.findViewById(R.id.lessonDescription)
        val startButton: Button = itemView.findViewById(R.id.lessonStartButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]

        with(holder) {
            title.text = lesson.title
            duration.text = "${lesson.durationMinutes} mins"
            xp.text = "${lesson.xpReward} XP"
            description.text = lesson.description

            startButton.setOnClickListener {
                onLessonClickListener(lesson)
            }
        }
    }


    override fun getItemCount(): Int = lessons.size
}