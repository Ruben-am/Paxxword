package com.rubenalba.paxxword.di

import com.rubenalba.paxxword.data.repository.AccountRepositoryImpl
import com.rubenalba.paxxword.data.repository.AuthRepositoryImpl
import com.rubenalba.paxxword.data.repository.FolderRepositoryImpl
import com.rubenalba.paxxword.domain.repository.AccountRepository
import com.rubenalba.paxxword.domain.repository.AuthRepository
import com.rubenalba.paxxword.domain.repository.FolderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(
        impl: FolderRepositoryImpl
    ): FolderRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}