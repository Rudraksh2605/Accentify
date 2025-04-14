package com.developerspoint.accentify.Home

import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.developerspoint.accentify.R
import com.developerspoint.accentify.NavBar.NavBar
import java.text.SimpleDateFormat
import java.util.Locale

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        navBar()
        date_time()
    }

    private fun navBar(){
        val navView = findViewById<View>(R.id.bottom_nav)
        NavBar(navView, this)
    }

    private fun date_time(){
        val time_date_txt = findViewById<TextView>(R.id.time_date_day)
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, MMMM, d 'at' hh:mm a", Locale.getDefault())
        time_date_txt.text = dateFormat.format(cal.time)
    }
}