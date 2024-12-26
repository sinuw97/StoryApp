package com.example.storyapp.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.databinding.ActivityStoriesDetailBinding
import com.example.storyapp.viewmodel.StoryViewModel

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoriesDetailBinding
    private val storyViewModel: StoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoriesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val storyId = intent.getStringExtra("story_id")


        if (storyId != null) {
            val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
            val token = preferences.getString("token", null)

            token?.let {

                storyViewModel.fetchStoryById(it, storyId)
            }


            storyViewModel.storyDetail.observe(this, Observer { story ->
                story?.let {

                    binding.tvStoryName.text = it.name
                    binding.tvStoryDescription.text = it.description
                    Glide.with(this)
                        .load(it.photoUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .into(binding.ivStoryThumbnail)
                }
            })
        }
    }
}
