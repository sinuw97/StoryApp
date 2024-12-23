package com.example.storyapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.storyapp.models.StoryRemoteKeys

@Dao
interface StoryRemoteKeysDao {

    @Query("SELECT * FROM story_remote_keys WHERE storyId = :id")
    suspend fun remoteKeysStoryId(id: String): StoryRemoteKeys?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<StoryRemoteKeys>)

    @Query("DELETE FROM story_remote_keys")
    suspend fun clearRemoteKeys()
}
