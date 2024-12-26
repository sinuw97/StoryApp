package com.example.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.storyapp.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnLihatStory = findViewById<Button>(R.id.btnLihatStory);
//        val btnAddStory = findViewById<Button>(R.id.btnAddStory);
        val btnLogOut = findViewById<Button>(R.id.btnLogOut);
        val tvWelcome = findViewById<TextView>(R.id.mainContent)




        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = preferences.getBoolean("is_logged_in", false)


        if (!isLoggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val username = preferences.getString("name", "User")
            tvWelcome.text = "Welcome to the Story App! $username"
        }

        // LogOut
        btnLogOut.setOnClickListener {
            val editor = preferences.edit()
            editor.remove("is_logged_in")
            editor.remove("user_token")
            editor.apply();

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnLihatStory.setOnClickListener{
            val intent = Intent(this, StoryActivity::class.java)
            startActivity(intent)
            finish()
        }

//
//        btnAddStory.setOnClickListener{
//
//        }
    }
}