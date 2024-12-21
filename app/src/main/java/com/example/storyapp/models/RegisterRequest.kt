package com.example.storyapp.models

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)