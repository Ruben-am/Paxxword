package com.rubenalba.paxxword.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rubenalba.paxxword.data.local.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM app_user LIMIT 1")
    suspend fun getAppUser(): User?

    @Query("DELETE FROM app_user")
    suspend fun deleteAllUsers()
}