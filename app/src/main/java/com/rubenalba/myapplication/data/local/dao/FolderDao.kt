package com.rubenalba.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rubenalba.myapplication.data.local.entity.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    // OnConflictStrategy.IGNORE: if you try to save a folder with an existent id, don't do nothing
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(folder: Folder): Long

    @Update
    suspend fun update(folder: Folder)

    @Delete
    suspend fun delete(folder: Folder)

    @Query("SELECT * FROM folders ORDER BY folder_name ASC")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Long): Folder?
}

// Personal notes:
// @Insert, @Update, @Delete (room automates queries), @Query custom query