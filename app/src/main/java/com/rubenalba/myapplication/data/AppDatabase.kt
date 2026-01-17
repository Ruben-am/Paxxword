package com.rubenalba.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rubenalba.myapplication.data.dao.AccountDao
import com.rubenalba.myapplication.data.dao.FolderDao
import com.rubenalba.myapplication.data.dao.UserDao
import com.rubenalba.myapplication.data.model.Account
import com.rubenalba.myapplication.data.model.Folder
import com.rubenalba.myapplication.data.model.User

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