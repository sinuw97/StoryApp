package com.example.storyapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengecek status login saat aplikasi dibuka
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = preferences.getBoolean("is_logged_in", false)  // default false jika tidak ditemukan

        // Jika sudah login, langsung ke MainActivity
        if (isLoggedIn) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // Jika belum login, ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        finish()  // Tutup SplashActivity setelah berpindah activity
    }
}