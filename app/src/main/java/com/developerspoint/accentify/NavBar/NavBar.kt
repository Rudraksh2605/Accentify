package com.developerspoint.accentify.NavBar

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageButton
import com.developerspoint.accentify.Course.CourseActivity
import com.developerspoint.accentify.Home.Home
import com.developerspoint.accentify.R
import com.developerspoint.accentify.Settings.Settings

class NavBar(private val rootView: View, private val activity: Activity) {

    init {
        setupNavigation()
    }

    private fun setupNavigation() {
        val historyBtn = rootView.findViewById<ImageButton>(R.id.history_btn)
        val homeBtn = rootView.findViewById<ImageButton>(R.id.home_btn)
        val profileBtn = rootView.findViewById<ImageButton>(R.id.profile_btn)
        val coursesBtn = rootView.findViewById<ImageButton>(R.id.courses_btn)

        homeBtn.setOnClickListener {
            if (activity !is Home) {
                activity.startActivity(Intent(activity, Home::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }

        profileBtn.setOnClickListener {
            if (activity !is Settings) {
                activity.startActivity(Intent(activity, Settings::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }

        coursesBtn.setOnClickListener {
            if (activity !is CourseActivity) {
                activity.startActivity(Intent(activity, CourseActivity::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }
    }
}