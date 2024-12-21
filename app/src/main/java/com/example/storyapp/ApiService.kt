package com.example.storyapp

import com.example.storyapp.models.LoginRequest
import com.example.storyapp.models.LoginResponse
import com.example.storyapp.models.RegisterRequest
import com.example.storyapp.models.RegisterResponse
import com.example.storyapp.models.StoryDetailResponse
import com.example.storyapp.models.StoryResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Endpoint untuk register
    @POST("register")
    fun register(@Body body: RegisterRequest): Call<RegisterResponse>

    // Endpoint untuk Login
    @POST("login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

    // Endpoint untuk all stories
    @GET("stories")
    fun getAllStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int? = 0
    ): Call<StoryResponse>

    // Endpoint untuk mengambil detail story berdasarkan ID
    @GET("stories/{id}")
    fun getStoryById(
        @Header("Authorization") token: String,
        @Path("id") id: String // id adalah parameter path yang akan diambil dari URL
    ): Call<StoryDetailResponse>
}