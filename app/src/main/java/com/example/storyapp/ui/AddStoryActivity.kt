package com.example.storyapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.storyapp.R
import androidx.appcompat.widget.Toolbar

class AddStoryActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        val toolbar: Toolbar = findViewById(R.id.addStoryBar)
        setSupportActionBar(toolbar)


    }
}