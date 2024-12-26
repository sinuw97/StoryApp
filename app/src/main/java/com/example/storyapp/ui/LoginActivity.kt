package com.example.storyapp.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.RetrofitClient
import com.example.storyapp.models.LoginRequest
import com.example.storyapp.models.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_login)

        val logoImageView = findViewById<ImageView>(R.id.logo_profile)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        Glide.with(this)
            .load(R.drawable.user1)
            .override(500, 500)
            .into(logoImageView)

        val moveLeft = ObjectAnimator.ofFloat(logoImageView, "translationX", -500f)
        val moveRight = ObjectAnimator.ofFloat(logoImageView, "translationX", 500f)

        moveLeft.duration = 15000
        moveRight.duration = 15000

        moveLeft.repeatCount = ObjectAnimator.INFINITE
        moveRight.repeatCount = ObjectAnimator.INFINITE

        moveLeft.repeatMode = ObjectAnimator.REVERSE
        moveRight.repeatMode = ObjectAnimator.REVERSE

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(moveLeft, moveRight)
        animatorSet.start()

        val emailAnimation = ObjectAnimator.ofFloat(etEmail, "alpha", 0f, 1f)
        val emailTranslate = ObjectAnimator.ofFloat(etEmail, "translationY", -500f, 0f)

        val passwordAnimation = ObjectAnimator.ofFloat(etPassword, "alpha", 0f, 1f)
        val passwordTranslate = ObjectAnimator.ofFloat(etPassword, "translationY", -500f, 0f)

        val btnLoginAnimation = ObjectAnimator.ofFloat(btnLogin, "alpha", 0f, 1f)
        val btnRegisterAnimation = ObjectAnimator.ofFloat(btnRegister, "alpha", 0f, 1f)

        emailAnimation.duration = 1000
        emailTranslate.duration = 1000
        passwordAnimation.duration = 1000
        passwordTranslate.duration = 1000
        btnLoginAnimation.duration = 1000
        btnRegisterAnimation.duration = 1000

        val animatorSet2 = AnimatorSet()
        animatorSet2.playTogether(
            emailAnimation, emailTranslate,
            passwordAnimation, passwordTranslate,
            btnLoginAnimation, btnRegisterAnimation
        )
        animatorSet2.start()

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi email dan password terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else {
                val loginRequest = LoginRequest(email, password)

                RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse?.error == false) {
                                val loginResult = loginResponse.loginResult
                                val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
                                val editor = preferences.edit()
                                editor.putString("userId", loginResult?.userId)
                                editor.putString("name", loginResult?.name)
                                editor.putString("token", loginResult?.token)
                                editor.putBoolean("is_logged_in", true)
                                editor.apply()

                                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    this@LoginActivity,
                                    logoImageView,
                                    ViewCompat.getTransitionName(logoImageView) ?: ""
                                )
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent, options.toBundle())
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Failed to authenticate. Please try again later.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
