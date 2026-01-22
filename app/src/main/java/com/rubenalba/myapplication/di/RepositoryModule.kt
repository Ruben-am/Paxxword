package com.rubenalba.myapplication.di

import com.rubenalba.myapplication.domain.repository.PasswordRepository
import com.rubenalba.myapplication.data.repository.PasswordRepositoryImpl
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
    abstract fun bindPasswordRepository(
        impl: PasswordRepositoryImpl
    ): PasswordRepository
}