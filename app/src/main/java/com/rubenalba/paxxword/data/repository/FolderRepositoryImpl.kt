package com.rubenalba.paxxword.data.repository

import com.rubenalba.paxxword.data.local.dao.FolderDao
import com.rubenalba.paxxword.data.mapper.FolderMapper
import com.rubenalba.paxxword.domain.model.FolderModel
import com.rubenalba.paxxword.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {
    override fun getAllFolders(): Flow<List<FolderModel>> =
        folderDao.getAllFolders().map { list -> list.map { FolderMapper.toDomain(it) } }

    override suspend fun insertFolder(folder: FolderModel): Long =
        folderDao.insert(FolderMapper.toEntity(folder))

    override suspend fun deleteFolder(folder: FolderModel) =
        folderDao.delete(FolderMapper.toEntity(folder))
}