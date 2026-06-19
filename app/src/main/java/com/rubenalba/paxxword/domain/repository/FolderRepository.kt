package com.rubenalba.paxxword.domain.repository

import com.rubenalba.paxxword.domain.model.FolderModel
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<FolderModel>>
    suspend fun insertFolder(folder: FolderModel): Long
    suspend fun deleteFolder(folder: FolderModel)
}