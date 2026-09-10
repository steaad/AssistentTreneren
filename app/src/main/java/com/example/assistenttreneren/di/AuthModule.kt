@file:Suppress("DEPRECATION")

package com.example.assistenttreneren.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.assistenttreneren.core.auth.EncryptedTokenStorage
import com.example.assistenttreneren.core.auth.AuthTokenRefresher
import com.example.assistenttreneren.core.auth.TokenStorage
import com.example.assistenttreneren.feature.login.data.repository.AuthTokenRefresherImpl
import com.example.assistenttreneren.feature.login.data.repository.AuthRepositoryImpl
import com.example.assistenttreneren.feature.login.domain.repository.AuthRepository
import com.example.assistenttreneren.feature.settings.data.repository.UserManagementRepositoryImpl
import com.example.assistenttreneren.feature.settings.domain.repository.UserManagementRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindTokenStorage(
        encryptedTokenStorage: EncryptedTokenStorage,
    ): TokenStorage

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAuthTokenRefresher(
        authTokenRefresherImpl: AuthTokenRefresherImpl,
    ): AuthTokenRefresher

    @Binds
    abstract fun bindUserManagementRepository(
        userManagementRepositoryImpl: UserManagementRepositoryImpl,
    ): UserManagementRepository

    companion object {
        @Provides
        @Singleton
        fun provideEncryptedSharedPreferences(
            @ApplicationContext context: Context,
        ): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                TOKEN_STORAGE_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }

        private const val TOKEN_STORAGE_FILE_NAME = "auth_tokens"
    }
}
