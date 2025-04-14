package com.developerspoint.accentify.Course

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.developerspoint.accentify.Lessons.Lessons
import com.developerspoint.accentify.Model.Course
import com.developerspoint.accentify.R

class CourseAdapter(private var courseList: List<Course>) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.course_title)
        val description: TextView = itemView.findViewById(R.id.course_desc)
        val difficulty: TextView = itemView.findViewById(R.id.difficulty_text)
        val xp: TextView = itemView.findViewById(R.id.xp_value)
        val lessons: TextView = itemView.findViewById(R.id.lessons_value)
        val progress: ProgressBar = itemView.findViewById(R.id.progress_bar)
        val startButton: Button = itemView.findViewById(R.id.action_button)
        val cardView: CardView = itemView.findViewById(R.id.course_card_root)





    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)



        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        Log.d("CourseAdapter", "Binding view holder at position: $position")

        val course = courseList[position]
        holder.title.text = course.title
        holder.description.text = course.description
        holder.difficulty.text = course.difficulty
        holder.xp.text = "${course.totalXp} XP"
        holder.lessons.text = "${course.totalLessons} lessons"

        holder.startButton.setOnClickListener {
            Log.d("CourseAdapter", "Card clicked: ${course.title}")
            val context = holder.cardView.context
            val intent = Intent(context, Lessons::class.java)
            intent.putExtra("courseId", course.id)
            intent.putExtra("courseTitle", course.title)
            intent.putExtra("courseDescription", course.description)
            intent.putExtra("courseDifficulty", course.difficulty)
            intent.putExtra("courseTotalXp", course.totalXp)
            intent.putExtra("courseTotalLessons", course.totalLessons)

            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = courseList.size

    fun updateList(newList: List<Course>){
        courseList = newList
        notifyDataSetChanged()
    }
}