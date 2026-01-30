package com.rubenalba.paxxword.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rubenalba.paxxword.data.local.dao.AccountDao
import com.rubenalba.paxxword.data.local.dao.FolderDao
import com.rubenalba.paxxword.data.local.dao.UserDao
import com.rubenalba.paxxword.data.local.entity.Account
import com.rubenalba.paxxword.data.local.entity.Folder
import com.rubenalba.paxxword.data.local.entity.User

@Database(
    entities = [Account::class, Folder::class, User::class], // The tables
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun folderDao(): FolderDao
    abstract fun userDao(): UserDao
}