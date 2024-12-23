package com.example.storyapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.storyapp.ApiService
import com.example.storyapp.db.StoryDatabase
import com.example.storyapp.models.Story
import com.example.storyapp.models.StoryRemoteKeys

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val apiService: ApiService,
    private val database: StoryDatabase,
    private val token: String
) : RemoteMediator<Int, Story>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Story>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state) ?: return MediatorResult.Success(
                    endOfPaginationReached = false
                )
                remoteKeys.nextKey
            }
        } ?: 1

        return try {
            val response = apiService.getAllStories("Bearer $token", page, state.config.pageSize)
            val stories = response.body()?.listStory.orEmpty()
            val endOfPagination = stories.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.storyRemoteKeysDao().clearRemoteKeys()
                    database.storyDao().clearStories()
                }

                val keys = stories.map {
                    StoryRemoteKeys(
                        storyId = it.id,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (endOfPagination) null else page + 1
                    )
                }
                database.storyRemoteKeysDao().insertAll(keys)
                database.storyDao().insertStories(stories)
            }
            MediatorResult.Success(endOfPaginationReached = endOfPagination)
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Story>): StoryRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { story -> database.storyRemoteKeysDao().remoteKeysStoryId(story.id) }
    }
}
