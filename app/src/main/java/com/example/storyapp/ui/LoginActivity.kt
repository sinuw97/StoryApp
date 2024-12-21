package com.example.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.storyapp.R
import com.example.storyapp.R.layout.activity_login
import com.example.storyapp.RetrofitClient
import com.example.storyapp.models.LoginRequest
import com.example.storyapp.models.LoginResponse
import com.example.storyapp.models.LoginResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mode terang
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail);
        val etPassword = findViewById<EditText>(R.id.etPassword);
        val btnLogin = findViewById<Button>(R.id.btnLogin);
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener{
            val email = etEmail.text.toString();
            val password = etPassword.text.toString();
            // Cek jika email dan password empty atau tidak
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                    "Isi email dan password terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show();
            } else {
                val loginRequest = LoginRequest(email, password)

                RetrofitClient.instance.login(loginRequest)
                    .enqueue(object : Callback<LoginResponse>{
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            if (response.body()?.error == false) {
                                // Save user data to SharedPreferences
                                val loginResult = response.body()?.loginResult
                                val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
                                val editor = preferences.edit()
                                editor.putString("userId", loginResult?.userId)
                                editor.putString("name", loginResult?.name)
                                editor.putString("token", loginResult?.token)
                                editor.putBoolean("is_logged_in", true)
                                editor.apply()

                                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Toast.makeText(this@LoginActivity, "Network error", Toast.LENGTH_SHORT).show()
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