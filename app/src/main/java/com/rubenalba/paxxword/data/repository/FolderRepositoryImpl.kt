package com.rubenalba.paxxword.data.repository

import com.rubenalba.paxxword.data.local.dao.FolderDao
import com.rubenalba.paxxword.data.local.entity.Folder
import com.rubenalba.paxxword.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {
    override fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()
    override suspend fun insertFolder(folder: Folder): Long = folderDao.insert(folder)
    override suspend fun deleteFolder(folder: Folder) = folderDao.delete(folder)
}