package com.developerspoint.accentify.Settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.developerspoint.accentify.Auth.Login
import com.developerspoint.accentify.Home.NavBar
import com.developerspoint.accentify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.developerspoint.accentify.Model.User
import com.google.firebase.firestore.SetOptions

class Settings : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var database: FirebaseDatabase


    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var editProfile: ImageButton
    private lateinit var progressBar: ProgressBar

    private lateinit var voiceSwitch: Switch
    private lateinit var grammarSwitch: Switch
    private lateinit var remindersSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews()

        setupBackButton()
        setupEditProfile()
        setupPreferences()
        setupLogout()

        navBar()
        fetchCurrentUser()

    }

    private fun setupViews() {
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        editProfile = findViewById(R.id.edit_profile)
        progressBar = findViewById(R.id.progressBar)


        voiceSwitch = findViewById(R.id.voice_switch)
        grammarSwitch = findViewById(R.id.grammar_switch)
        remindersSwitch = findViewById(R.id.reminders_switch)
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupEditProfile() {
        editProfile.setOnClickListener {

            val picname = findViewById<LinearLayout>(R.id.orignal_tv_prof)
            picname.visibility = if(picname.visibility == View.VISIBLE){
                View.GONE
            }else{
                View.VISIBLE
            }
            val editContainer = findViewById<LinearLayout>(R.id.edit_mode_container)
            editContainer.visibility = if (editContainer.visibility == View.VISIBLE) {
                View.GONE
            } else {
                findViewById<EditText>(R.id.et_name).setText(tvName.text)
                findViewById<EditText>(R.id.et_email).setText(tvEmail.text)
                View.VISIBLE
            }
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            saveProfileChanges()
            val picname = findViewById<LinearLayout>(R.id.orignal_tv_prof)
            picname.visibility = if(picname.visibility == View.VISIBLE){
                View.GONE
            }else{
                View.VISIBLE
            }
        }

        findViewById<ImageButton>(R.id.btn_change_photo).setOnClickListener {
            Toast.makeText(this, "Change photo functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navBar() {
        val navView = findViewById<View>(R.id.bottom_nav)
        NavBar(navView, this)
    }

    private fun fetchCurrentUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId)

            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userData = documentSnapshot.toObject(User::class.java)
                        userData?.let {
                            tvName.text = it.name
                            tvEmail.text = it.email
                            voiceSwitch.isChecked = it.preferences.voiceInput
                            grammarSwitch.isChecked = it.preferences.grammarTips
                            remindersSwitch.isChecked = it.preferences.dailyReminders
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Error", exception.toString())
                }
        }
    }

    private fun saveProfileChanges() {
        val newName = findViewById<EditText>(R.id.et_name).text.toString().trim()
        val newEmail = findViewById<EditText>(R.id.et_email).text.toString().trim()

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        val userId = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>(
            "name" to newName,
            "email" to newEmail
        )

        db.collection("Users").document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                showLoading(false)
                tvName.text = newName
                tvEmail.text = newEmail
                findViewById<LinearLayout>(R.id.edit_mode_container).visibility = View.GONE
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                // Update email in Firebase Auth if changed
                if (newEmail != auth.currentUser?.email) {
                    auth.currentUser?.updateEmail(newEmail)
                        ?.addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(this,
                                    "Email updated in profile but failed to update authentication email",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupPreferences() {
        voiceSwitch.setOnCheckedChangeListener { _, isChecked ->
            savePreference("preferences.voiceInput", isChecked)
        }

        grammarSwitch.setOnCheckedChangeListener { _, isChecked ->
            savePreference("preferences.grammarTips", isChecked)
        }

        remindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            savePreference("preferences.dailyReminders", isChecked)
        }
    }

    private fun savePreference(field: String, value: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>(field to value)

        db.collection("Users").document(userId)
            .set(updates, SetOptions.merge())
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save preference", Toast.LENGTH_SHORT).show()
                Log.e("Settings", "Error saving preference", e)
            }
    }

    private fun setupLogout() {
        findViewById<Button>(R.id.logout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(this, Login::class.java))
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }


}
