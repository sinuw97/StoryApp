package com.example.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.RetrofitClient
import com.example.storyapp.models.Story
import com.example.storyapp.models.StoryDetailResponse
import com.example.storyapp.models.StoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryViewModel : ViewModel() {

    private val _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> get() = _stories


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage


    private val _storyDetail = MutableLiveData<Story>()
    val storyDetail: LiveData<Story> get() = _storyDetail


    fun fetchStories(token: String, page: Int? = null, size: Int? = null, location: Int? = 0) {
        RetrofitClient.instance.getAllStories("Bearer $token", page, size, location)
            .enqueue(object : Callback<StoryResponse> {
                override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                    if (response.isSuccessful) {
                        _stories.postValue(response.body()?.listStory)
                    } else {
                        _errorMessage.postValue("Failed to fetch stories: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                    _errorMessage.postValue("Error: ${t.message}")
                }
            })
    }

    fun fetchStoryById(token: String, id: String) {
        RetrofitClient.instance.getStoryById("Bearer $token", id)
            .enqueue(object : Callback<StoryDetailResponse> {
                override fun onResponse(call: Call<StoryDetailResponse>, response: Response<StoryDetailResponse>) =
                    if (response.isSuccessful) {
                        val story = response.body()?.story
                        if (story != null) {
                            _storyDetail.postValue(story)
                        } else {
                            _errorMessage.postValue("Story not found")
                        }
                    } else {
                        _errorMessage.postValue("Failed to fetch story: ${response.message()}")
                    }

                override fun onFailure(call: Call<StoryDetailResponse>, t: Throwable) {
                    _errorMessage.postValue("Error: ${t.message}")
                }
            })
    }
}