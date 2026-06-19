package com.rubenalba.paxxword.domain.usecase

import com.rubenalba.paxxword.domain.model.FolderModel
import com.rubenalba.paxxword.domain.repository.FolderRepository
import javax.inject.Inject

class GetFoldersUseCase @Inject constructor(private val repo: FolderRepository) {
    operator fun invoke() = repo.getAllFolders()
}
class AddFolderUseCase @Inject constructor(private val repo: FolderRepository) {
    suspend operator fun invoke(folder: FolderModel) = repo.insertFolder(folder)
}
class DeleteFolderUseCase @Inject constructor(private val repo: FolderRepository) {
    suspend operator fun invoke(folder: FolderModel) = repo.deleteFolder(folder)
}