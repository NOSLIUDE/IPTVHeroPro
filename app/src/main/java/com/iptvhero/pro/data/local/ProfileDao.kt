package com.iptvhero.pro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY isActive DESC, lastUsed DESC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("UPDATE profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE profiles SET isActive = 1, lastUsed = :timestamp WHERE id = :profileId")
    suspend fun activateProfileById(profileId: Long, timestamp: Long = System.currentTimeMillis())

    @Transaction
    suspend fun setActiveProfile(profileId: Long) {
        deactivateAllProfiles()
        activateProfileById(profileId)
    }

    @Query("UPDATE profiles SET liveCount = :live, vodCount = :vod, seriesCount = :series WHERE id = :profileId")
    suspend fun updateCounts(profileId: Long, live: Int, vod: Int, series: Int)
}
