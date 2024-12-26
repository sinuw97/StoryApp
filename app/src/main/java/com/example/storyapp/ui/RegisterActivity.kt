package com.example.storyapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.R.layout.activity_register
import com.example.storyapp.RetrofitClient
import com.example.storyapp.models.RegisterRequest
import com.example.storyapp.models.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mode terang
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(activity_register)
        // Load gambar menggunakan Glide
        Glide.with(this)
            .load(R.drawable.pemain) // Ganti dengan nama gambar yang ada di drawable
            .into(findViewById<ImageView>(R.id.icon_image))

        val etName = findViewById<EditText>(R.id.etName);
        val etEmail = findViewById<EditText>(R.id.etEmail);
        val etPassword = findViewById<EditText>(R.id.etPassword);
        val btnRegister = findViewById<Button>(R.id.btnRegister);

        btnRegister.setOnClickListener {
            val name = etName.text.toString();
            val email = etEmail.text.toString();
            val password = etPassword.text.toString();

            if (name.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Di isi dulu mas bro", Toast.LENGTH_SHORT).show()
            } else if (password.length < 8) {
                Toast.makeText(this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
            } else {
                val registerRequest = RegisterRequest(name, email, password)

                RetrofitClient.instance.register(registerRequest)
                    .enqueue(object : Callback<RegisterResponse> {
                        override fun onResponse(
                            call: Call<RegisterResponse>,
                            response: Response<RegisterResponse>
                        ) {
                            if (response.body()?.error == false) {
                                Toast.makeText(this@RegisterActivity, "User Created", Toast.LENGTH_SHORT).show()
                                // Arahkan ke login page
                                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@RegisterActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                            Toast.makeText(this@RegisterActivity, "Network error", Toast.LENGTH_SHORT).show()
                        }
                    })
            }


        }
    }
}