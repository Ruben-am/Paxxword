package com.rubenalba.paxxword.data.mapper

import com.rubenalba.paxxword.data.local.entity.Folder
import com.rubenalba.paxxword.domain.model.FolderModel

object FolderMapper {
    fun toDomain(entity: Folder) = FolderModel(id = entity.id, name = entity.folderName)
    fun toEntity(model: FolderModel) = Folder(id = model.id, folderName = model.name)
}