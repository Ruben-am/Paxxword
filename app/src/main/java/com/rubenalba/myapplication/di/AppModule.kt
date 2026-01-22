package com.rubenalba.myapplication.di

import android.app.Application
import androidx.room.Room
import com.rubenalba.myapplication.data.local.AppDatabase
import com.rubenalba.myapplication.data.local.dao.AccountDao
import com.rubenalba.myapplication.data.local.dao.FolderDao
import com.rubenalba.myapplication.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "paxxword_db" // name of .bd in mobile storage
        ).build()
    }

    @Provides
    @Singleton
    fun provideAccountDao(db: AppDatabase): AccountDao {
        return db.accountDao()
    }

    @Provides
    @Singleton
    fun provideFolderDao(db: AppDatabase): FolderDao {
        return db.folderDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao {
        return db.userDao()
    }
}