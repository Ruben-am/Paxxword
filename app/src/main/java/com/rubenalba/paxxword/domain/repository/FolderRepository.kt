package com.rubenalba.paxxword.domain.repository

import com.rubenalba.paxxword.data.local.entity.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    suspend fun insertFolder(folder: Folder): Long
    suspend fun deleteFolder(folder: Folder)
}