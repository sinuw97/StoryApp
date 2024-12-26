package com.example.storyapp.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.example.storyapp.ApiService
import com.example.storyapp.RetrofitClient
import com.example.storyapp.models.Story
import com.example.storyapp.models.StoryDetailResponse
import com.example.storyapp.paging.StoryPagingSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryViewModel(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    // Ambil token dari sharedPreferences
    private val token: String
        get() = sharedPreferences.getString("token", "") ?: ""

    // LiveData untuk paging data
    private val _stories = MutableLiveData<PagingData<Story>>()
    val stories: LiveData<PagingData<Story>> get() = _stories

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _storyDetail = MutableLiveData<Story>()
    val storyDetail: LiveData<Story> get() = _storyDetail

    fun fetchStories(token: String, page: Int, size: Int) {
        val pagingConfig = PagingConfig(
            pageSize = size,
            enablePlaceholders = false
        )
        val pager = Pager(
            config = pagingConfig,
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        )

        pager.liveData.cachedIn(viewModelScope).observeForever {
            _stories.postValue(it)
        }
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
