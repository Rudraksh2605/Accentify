package com.developerspoint.accentify.Settings

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.developerspoint.accentify.Auth.Login
import com.developerspoint.accentify.NavBar.NavBar
import com.developerspoint.accentify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class Settings : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    // Profile Views
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var editProfile: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var profileImageView: CircleImageView
    private lateinit var profileImageEditView: CircleImageView

    // Preferences Views
    private lateinit var voiceSwitch: Switch
    private lateinit var grammarSwitch: Switch
    private lateinit var remindersSwitch: Switch



    // Accessibility Views
    private lateinit var fontSizeText: TextView
    private lateinit var contrastText: TextView

    // Constants
    private companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val TAG = "SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        // Initialize views
        setupViews()

        // Setup click listeners
        setupBackButton()
        setupEditProfile()
        setupPreferences()
        setupAccessibility()
        setupLogout()

        // Setup navigation
        navBar()

        // Fetch current user data
        fetchCurrentUser()
    }

    private fun setupViews() {
        // Profile views
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        editProfile = findViewById(R.id.edit_profile)
        progressBar = findViewById(R.id.progressBar)
        profileImageView = findViewById(R.id.iv_profile_pic)
        profileImageEditView = findViewById(R.id.iv_profile_pic_edit)

        // Preference switches
        voiceSwitch = findViewById(R.id.voice_switch)
        grammarSwitch = findViewById(R.id.grammar_switch)
        remindersSwitch = findViewById(R.id.reminders_switch)


        // Accessibility
        fontSizeText = findViewById(R.id.font_size)
        contrastText = findViewById(R.id.contrast_text)
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupEditProfile() {
        editProfile.setOnClickListener {
            toggleEditProfileMode()
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            saveProfileChanges()
        }

        findViewById<ImageButton>(R.id.btn_change_photo).setOnClickListener {
            openImagePicker()
        }
    }

    private fun toggleEditProfileMode() {
        val profileView = findViewById<LinearLayout>(R.id.orignal_tv_prof)
        val editContainer = findViewById<LinearLayout>(R.id.edit_mode_container)

        if (editContainer.visibility == View.VISIBLE) {
            editContainer.visibility = View.GONE
            profileView.visibility = View.VISIBLE
        } else {
            profileView.visibility = View.GONE
            editContainer.visibility = View.VISIBLE
            findViewById<EditText>(R.id.et_name).setText(tvName.text)
            findViewById<EditText>(R.id.et_email).setText(tvEmail.text)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                // Display the selected image
                Glide.with(this)
                    .load(it)
                    .into(profileImageEditView)

                // Upload the image to Firebase Storage
                uploadProfileImage(it)
            }
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        showLoading(true)
        val userId = auth.currentUser?.uid ?: return
        val imageRef = storageRef.child("profile_images/$userId.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Get the download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Save the URL to Firestore
                    saveProfileImageUrl(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileImageUrl(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>("profileImageUrl" to imageUrl)

        db.collection("Users").document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                showLoading(false)
                // Update the profile image view
                Glide.with(this)
                    .load(imageUrl)
                    .into(profileImageView)
                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Failed to save image URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navBar() {
        val navView = findViewById<View>(R.id.bottom_nav)
        NavBar(navView, this)
    }

    private fun fetchCurrentUser() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Update profile info
                    tvName.text = document.getString("name") ?: "No Name"
                    tvEmail.text = document.getString("email") ?: "No Email"

                    // Update profile image if available
                    document.getString("profileImageUrl")?.let { imageUrl ->
                        Glide.with(this)
                            .load(imageUrl)
                            .into(profileImageView)
                        Glide.with(this)
                            .load(imageUrl)
                            .into(profileImageEditView)
                    }

                    // Update preferences
                    val preferences = document.get("preferences") as? Map<*, *>
                    preferences?.let {
                        voiceSwitch.isChecked = it["voiceInput"] as? Boolean ?: false
                        grammarSwitch.isChecked = it["grammarTips"] as? Boolean ?: false
                        remindersSwitch.isChecked = it["dailyReminders"] as? Boolean ?: false
                    }

                    // Update accessibility settings
                    val fontSize = document.getString("fontSize") ?: "Medium"
                    val contrast = document.getString("contrast") ?: "Normal"
                    fontSizeText.text = fontSize
                    contrastText.text = contrast
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching user data", e)
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
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
                toggleEditProfileMode()
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                // Update email in Firebase Auth if changed
                if (newEmail != auth.currentUser?.email) {
                    auth.currentUser?.updateEmail(newEmail)
                        ?.addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Email updated in profile but failed to update authentication email",
                                    Toast.LENGTH_LONG
                                ).show()
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
                Log.e(TAG, "Error saving preference", e)
                // Revert the UI change if save failed
                when (field) {
                    "preferences.voiceInput" -> voiceSwitch.isChecked = !value
                    "preferences.grammarTips" -> grammarSwitch.isChecked = !value
                    "preferences.dailyReminders" -> remindersSwitch.isChecked = !value
                }
            }
    }

    private fun setupAccessibility() {
        // Font size selection
        fontSizeText.setOnClickListener {
            showFontSizeDialog()
        }

        // Contrast selection
        contrastText.setOnClickListener {
            showContrastDialog()
        }
    }

    private fun showFontSizeDialog() {
        val fontSizes = arrayOf("Small", "Medium", "Large")
        val currentSize = fontSizeText.text.toString()

        AlertDialog.Builder(this)
            .setTitle("Select Font Size")
            .setSingleChoiceItems(fontSizes, fontSizes.indexOf(currentSize)) { dialog, which ->
                val selectedSize = fontSizes[which]
                saveAccessibilitySetting("fontSize", selectedSize)
                fontSizeText.text = selectedSize
                dialog.dismiss()
                // Apply font size changes throughout the app
                applyFontSize(selectedSize)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showContrastDialog() {
        val contrastOptions = arrayOf("Normal", "High")
        val currentContrast = contrastText.text.toString()

        AlertDialog.Builder(this)
            .setTitle("Select Contrast")
            .setSingleChoiceItems(contrastOptions, contrastOptions.indexOf(currentContrast)) { dialog, which ->
                val selectedContrast = contrastOptions[which]
                saveAccessibilitySetting("contrast", selectedContrast)
                contrastText.text = selectedContrast
                dialog.dismiss()
                // Apply contrast changes
                applyContrast(selectedContrast)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveAccessibilitySetting(field: String, value: String) {
        val userId = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>(field to value)

        db.collection("Users").document(userId)
            .set(updates, SetOptions.merge())
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save setting", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error saving accessibility setting", e)
            }
    }

    private fun applyFontSize(size: String) {
        // Implement font size changes throughout the app
        // This would typically involve setting a global theme or preference
        // that other activities would read when they start
        Toast.makeText(this, "Font size changed to $size. Restart app for full effect.", Toast.LENGTH_SHORT).show()
    }

    private fun applyContrast(contrast: String) {
        when (contrast) {
            "High" -> {
                // Apply high contrast theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                // Apply normal contrast theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        recreate() // Restart activity to apply changes
    }

    private fun setupLogout() {
        findViewById<Button>(R.id.logout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(this, Login::class.java))
                    finishAffinity() // Close all activities
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            findViewById<LinearLayout>(R.id.edit_mode_container).alpha = 0.5f
            findViewById<LinearLayout>(R.id.edit_mode_container).isEnabled = false
        } else {
            findViewById<LinearLayout>(R.id.edit_mode_container).alpha = 1f
            findViewById<LinearLayout>(R.id.edit_mode_container).isEnabled = true
        }
    }
}