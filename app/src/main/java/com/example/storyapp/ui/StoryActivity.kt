package com.example.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.adapter.StoryAdapter
import com.example.storyapp.databinding.ActivityStoryListBinding
import com.example.storyapp.viewmodel.StoryViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryListBinding
    private val storyViewModel: StoryViewModel by viewModels()
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aktifkan toolbar
        setSupportActionBar(binding.listToolBar)

        setupRecyclerView()
        setupObservers()
        setupFabAnimation()
        loadStories()
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter(listOf()) { story ->
            val intent = Intent(this, StoryDetailActivity::class.java).apply {
                putExtra("story_id", story.id)
            }
            startActivity(intent)
        }
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@StoryActivity)
            adapter = storyAdapter
        }
    }

    private fun setupObservers() {
        storyViewModel.stories.observe(this, Observer { stories ->
            if (stories != null) {
                storyAdapter = StoryAdapter(stories) { story ->
                    val intent = Intent(this, StoryDetailActivity::class.java).apply {
                        putExtra("story_id", story.id)
                    }
                    startActivity(intent)
                }
                binding.rvStories.adapter = storyAdapter

                // Add fade-in animation for RecyclerView
                binding.rvStories.alpha = 0f
                binding.rvStories.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start()
            }
        })

        storyViewModel.errorMessage.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupFabAnimation() {
        val fab: FloatingActionButton = binding.btnAddStory

        fab.setOnClickListener {
            // Scale down animation
            fab.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(100)
                .withEndAction {
                    // Scale up animation
                    fab.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            // Start AddStoryActivity with custom animation
                            val intent = Intent(this, AddStoryActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
                        }
                }
        }
    }

    private fun loadStories() {
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = preferences.getString("token", null)
        if (token != null) {
            storyViewModel.fetchStories(token, page = 1, size = 15)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_story, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_refresh -> {
                animateRefresh()
                true
            }
            R.id.menu_logout -> {
                logout()
                true
            }
            R.id.menu_maps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun animateRefresh() {
        // Fade out animation for RecyclerView
        binding.rvStories.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                refreshStories()
            }
    }

    private fun refreshStories() {
        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = preferences.getString("token", null)
        if (token != null) {
            storyViewModel.fetchStories(token, page = 1, size = 15)
        } else {
            Toast.makeText(this, "Token tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        // Fade out animation before logout
        binding.root.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                getSharedPreferences("user_session", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
    }

    // Handle animation when returning from AddStoryActivity
    override fun onResume() {
        super.onResume()
        binding.btnAddStory.apply {
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        }
    }
}