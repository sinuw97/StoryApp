package com.example.storyapp.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.RetrofitClient
import com.example.storyapp.databinding.ActivityStoriesDetailBinding
import com.example.storyapp.viewmodel.StoryViewModel
import com.example.storyapp.viewmodel.StoryViewModelFactory

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoriesDetailBinding
    private lateinit var storyViewModel: StoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoriesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra("story_id")

        Log.d("StoryDetailActivity", "Received Story ID: $storyId")

        val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = preferences.getString("token", null)

        val apiService = RetrofitClient.instance // Assuming you have a method to create ApiService instance
        storyViewModel = ViewModelProvider(
            this,
            StoryViewModelFactory(apiService, preferences)
        ).get(StoryViewModel::class.java)

        if (storyId != null) {
            Log.d("StoryDetailActivity", "Received Token: $token")

            token?.let {
                storyViewModel.fetchStoryById(it, storyId)
            }

            storyViewModel.storyDetail.observe(this, Observer { story ->
                if (story != null) {
                    Log.d("StoryDetailActivity", "Story received: ${story.name}")
                    binding.tvStoryName.text = story.name
                    binding.tvStoryDescription.text = story.description
                    Glide.with(this)
                        .load(story.photoUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .into(binding.ivStoryThumbnail)
                } else {
                    Log.e("StoryDetailActivity", "Story not found or null")
                }
            })

            storyViewModel.errorMessage.observe(this, Observer { error ->
                error?.let {
                    Log.e("StoryDetailActivity", "Error fetching story: $it")
                }
            })
        } else {
            Log.e("StoryDetailActivity", "Story ID is null")
        }
    }
}
