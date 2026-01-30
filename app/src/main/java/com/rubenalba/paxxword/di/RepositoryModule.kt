package com.rubenalba.paxxword.di

import com.rubenalba.paxxword.domain.repository.PasswordRepository
import com.rubenalba.paxxword.data.repository.PasswordRepositoryImpl
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