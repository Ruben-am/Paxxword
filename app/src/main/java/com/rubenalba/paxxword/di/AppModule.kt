package com.rubenalba.paxxword.di

import android.app.Application
import androidx.room.Room
import com.rubenalba.paxxword.data.local.AppDatabase
import com.rubenalba.paxxword.data.local.dao.AccountDao
import com.rubenalba.paxxword.data.local.dao.FolderDao
import com.rubenalba.paxxword.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE accounts ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "paxxword_db" // name of .bd in mobile storage
        )
            .addMigrations(MIGRATION_1_2)
            .build()
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