package com.example.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.adapter.StoryAdapter
import com.example.storyapp.databinding.ActivityStoryListBinding
import com.example.storyapp.viewmodel.StoryViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class StoryActivity: AppCompatActivity() {

    private lateinit var binding: ActivityStoryListBinding
    private val storyViewModel: StoryViewModel by viewModels()

    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recycler View
        storyAdapter = StoryAdapter(listOf()) { story ->
            // Ketika item story diklik, buka StoryDetailActivity
            val intent = Intent(this, StoryDetailActivity::class.java).apply {
                putExtra("story_id", story.id)
            }
            startActivity(intent)
        }
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.adapter = storyAdapter

        // get token
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = preferences.getString("token", null);

        storyViewModel.stories.observe(this, Observer { stories ->
            if (stories != null) {
                storyAdapter = StoryAdapter(stories) { story ->
                    // Klik item akan membuka StoryDetailActivity
                    val intent = Intent(this, StoryDetailActivity::class.java).apply {
                        putExtra("story_id", story.id)
                    }
                    startActivity(intent)
                }
                binding.rvStories.adapter = storyAdapter
            }
        })

        storyViewModel.errorMessage.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        })

        if (token != null) {
            storyViewModel.fetchStories(token, page = 1, size = 15)
        }

        val btnAddStory: FloatingActionButton = findViewById(R.id.btnAddStory)
        btnAddStory.setOnClickListener{
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }
}