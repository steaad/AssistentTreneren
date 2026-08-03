package com.example.assistenttreneren.di

import com.example.assistenttreneren.BuildConfig
import com.example.assistenttreneren.core.network.AuthAuthenticator
import com.example.assistenttreneren.core.network.AuthInterceptor
import com.example.assistenttreneren.feature.activitywizard.data.remote.CoachActivityApi
import com.example.assistenttreneren.feature.analysis.data.remote.AnalysisApi
import com.example.assistenttreneren.feature.login.data.remote.AuthApi
import com.example.assistenttreneren.feature.transcription.data.remote.TranscriptionReviewApi
import com.example.assistenttreneren.feature.upload.data.remote.UploadApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    @Named(NO_AUTH_CLIENT)
    fun provideNoAuthOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request()
                        .newBuilder()
                        .removeHeader(AuthInterceptor.NO_AUTH_HEADER)
                        .build(),
                )
            }
            .addInterceptor(provideLoggingInterceptor())
            .build()

    @Provides
    @Singleton
    @Named(AUTHENTICATED_CLIENT)
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
        authAuthenticator: AuthAuthenticator,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(authAuthenticator)
            .addInterceptor(provideLoggingInterceptor())
            .build()

    @Provides
    @Singleton
    @Named(NO_AUTH_RETROFIT)
    fun provideNoAuthRetrofit(
        @Named(NO_AUTH_CLIENT)
        okHttpClient: OkHttpClient,
    ): Retrofit = buildRetrofit(okHttpClient)

    @Provides
    @Singleton
    @Named(AUTHENTICATED_RETROFIT)
    fun provideAuthenticatedRetrofit(
        @Named(AUTHENTICATED_CLIENT)
        okHttpClient: OkHttpClient,
    ): Retrofit = buildRetrofit(okHttpClient)

    @Provides
    @Singleton
    fun provideAuthApi(
        @Named(NO_AUTH_RETROFIT)
        retrofit: Retrofit,
    ): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideCoachActivityApi(
        @Named(AUTHENTICATED_RETROFIT)
        retrofit: Retrofit,
    ): CoachActivityApi = retrofit.create(CoachActivityApi::class.java)

    @Provides
    @Singleton
    fun provideUploadApi(
        @Named(AUTHENTICATED_RETROFIT)
        retrofit: Retrofit,
    ): UploadApi = retrofit.create(UploadApi::class.java)

    @Provides
    @Singleton
    fun provideAnalysisApi(
        @Named(AUTHENTICATED_RETROFIT)
        retrofit: Retrofit,
    ): AnalysisApi = retrofit.create(AnalysisApi::class.java)

    @Provides
    @Singleton
    fun provideTranscriptionReviewApi(
        @Named(AUTHENTICATED_RETROFIT)
        retrofit: Retrofit,
    ): TranscriptionReviewApi = retrofit.create(TranscriptionReviewApi::class.java)

    private fun buildRetrofit(
        okHttpClient: OkHttpClient,
    ): Retrofit {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    private const val NO_AUTH_CLIENT = "NoAuthClient"
    private const val AUTHENTICATED_CLIENT = "AuthenticatedClient"
    private const val NO_AUTH_RETROFIT = "NoAuthRetrofit"
    private const val AUTHENTICATED_RETROFIT = "AuthenticatedRetrofit"
}
