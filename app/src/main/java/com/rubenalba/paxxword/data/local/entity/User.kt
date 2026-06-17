package com.rubenalba.paxxword.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_user")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_email")
    val userEmail: String,

    @ColumnInfo(name = "user_salt")
    val userSalt: String,

    @ColumnInfo(name = "verification_token")
    val verificationToken: String,

    @ColumnInfo(name = "encrypted_verification_value")
    val encryptedVerificationValue: String,

    @ColumnInfo(name = "iv_verification_value")
    val ivVerificationValue: String
)