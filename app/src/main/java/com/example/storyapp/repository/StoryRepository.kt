package com.example.storyapp.repository

import com.example.storyapp.ApiService
import com.example.storyapp.database.StoryDatabase
import com.example.storyapp.models.Story

class StoryRepository(
    val apiService: ApiService,
    private val storyDatabase: StoryDatabase
) {
    suspend fun saveStories(stories: List<Story>, clearExisting: Boolean) {
        val storyDao = storyDatabase.storyDao()
        storyDatabase.runInTransaction {
            if (clearExisting) {
                storyDao.clearAll()
            }
            storyDao.insertAll(stories)
        }ꦕ
    }

    fun getStoryDatabase(): StoryDatabase = storyDatabase
}
