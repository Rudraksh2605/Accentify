package com.developerspoint.accentify.Auth

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.developerspoint.accentify.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.developerspoint.accentify.Home.Home
import androidx.core.content.ContextCompat

class SignUp : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(this@SignUp, android.R.color.transparent)
            setBackgroundDrawable(ContextCompat.getDrawable(this@SignUp, R.color.white))
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()


        val etUsername = findViewById<EditText>(R.id.et_username)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)

        val googleButton = findViewById<Button>(R.id.btn_google_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up Google login button
        googleButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        val signupBtn = findViewById<Button>(R.id.btn_login)
        signupBtn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && password.length >= 6) {
                createAccount(email, password)
            } else {
                Toast.makeText(
                    this,
                    "Please provide a valid email and password (min 6 characters).",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SignUp", "Account created for email: $email")
                    showWelcomeDialog()
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Account creation failed."
                    Log.e("SignUp", "Error: ${task.exception}", task.exception)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed: ${e.statusCode}", e)
                Toast.makeText(this, "Google Sign-In failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "Google sign-in successful")
                    showWelcomeDialog()
                } else {
                    Toast.makeText(this, "Google Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("GoogleSignIn", "Firebase sign-in failed", task.exception)
                }
            }
    }

    private fun showWelcomeDialog(title: String = "Welcome!", message: String = "Your account has been created successfully!") {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.welocme_signup)
        dialog.setCancelable(false)

        val textTitle = dialog.findViewById<TextView>(R.id.textTitle)
        val textSubtitle = dialog.findViewById<TextView>(R.id.textSubtitle)
        val buttonGoToHome = dialog.findViewById<Button>(R.id.buttonGoToHome)


        textTitle.text = title
        textSubtitle.text = message

        buttonGoToHome.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}