package com.example.storyapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storyapp.ApiService
import com.example.storyapp.models.Story

class StoryPagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, Story>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val position = params.key ?: 1
            val response = apiService.getPagedStories("Bearer $token", position, params.loadSize)
            LoadResult.Page(
                data = response.listStory,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (response.listStory.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
