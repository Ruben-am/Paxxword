package com.rubenalba.paxxword.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL // if the folder is deleted, folder id = null
        )
    ]
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "folder_id", index = true)
    val folderId: Long? = null, // can be null (account without folder)

    @ColumnInfo(name = "service_name")
    val serviceName: String,

    @ColumnInfo(name = "encrypted_email")
    val encryptedEmail: String? = null,

    @ColumnInfo(name = "iv_email")
    val ivEmail: String? = null,

    @ColumnInfo(name = "encrypted_username")
    val encryptedUsername: String? = null,

    @ColumnInfo(name = "iv_username")
    val ivUsername: String? = null,

    @ColumnInfo(name = "encrypted_password")
    val encryptedPassword: String,

    @ColumnInfo(name = "iv_password")
    val ivPassword: String,

    @ColumnInfo(name = "encrypted_url")
    val encryptedUrl: String? = null,

    @ColumnInfo(name = "iv_url")
    val ivUrl: String? = null,

    @ColumnInfo(name = "encrypted_notes")
    val encryptedNotes: String? = null,

    @ColumnInfo(name = "iv_notes")
    val ivNotes: String? = null,

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis()
)
