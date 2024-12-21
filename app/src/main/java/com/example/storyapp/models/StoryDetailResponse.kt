package com.example.storyapp.models

data class StoryDetailResponse(
    val error: Boolean,
    val message: String,
    val story: Story
)
