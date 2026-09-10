package com.example.assistenttreneren.di

import android.util.Log
import com.example.assistenttreneren.BuildConfig
import com.example.assistenttreneren.core.network.AuthAuthenticator
import com.example.assistenttreneren.core.network.AuthInterceptor
import com.example.assistenttreneren.feature.activitywizard.data.remote.CoachActivityApi
import com.example.assistenttreneren.feature.analysis.data.remote.AnalysisApi
import com.example.assistenttreneren.feature.login.data.remote.AuthApi
import com.example.assistenttreneren.feature.transcription.data.remote.TranscriptionReviewApi
import com.example.assistenttreneren.feature.upload.data.remote.UploadApi
import com.example.assistenttreneren.feature.settings.data.remote.UserManagementApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
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
            .addInterceptor(provideTranscriptionReviewLoggingInterceptor())
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
            .addInterceptor(provideTranscriptionReviewLoggingInterceptor())
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

    @Provides
    @Singleton
    fun provideUserManagementApi(
        @Named(AUTHENTICATED_RETROFIT) retrofit: Retrofit,
    ): UserManagementApi = retrofit.create(UserManagementApi::class.java)

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

    private fun provideTranscriptionReviewLoggingInterceptor(): Interceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        if (
            BuildConfig.DEBUG &&
            response.isSuccessful &&
            chain.request().url.encodedPath.endsWith("/transcription-review")
        ) {
            val responseBody = response.peekBody(MAX_TRANSCRIPTION_REVIEW_LOG_BYTES).string()
            logTranscriptionReviewResponse(responseBody)
        }
        response
    }

    private fun logTranscriptionReviewResponse(responseBody: String) {
        if (responseBody.isEmpty()) {
            Log.d(TRANSCRIPTION_REVIEW_LOG_TAG, "Tom transcription-review-respons")
            return
        }

        val formattedResponseBody = runCatching {
            JSONObject(responseBody).toString(JSON_INDENT_SPACES)
        }.getOrDefault(responseBody)

        formattedResponseBody.chunked(LOG_CHUNK_SIZE).forEachIndexed { index, chunk ->
            Log.d(TRANSCRIPTION_REVIEW_LOG_TAG, "transcription-review JSON del ${index + 1}: $chunk")
        }
    }

    private const val NO_AUTH_CLIENT = "NoAuthClient"
    private const val AUTHENTICATED_CLIENT = "AuthenticatedClient"
    private const val NO_AUTH_RETROFIT = "NoAuthRetrofit"
    private const val AUTHENTICATED_RETROFIT = "AuthenticatedRetrofit"
    private const val TRANSCRIPTION_REVIEW_LOG_TAG = "TranscriptionReview"
    private const val MAX_TRANSCRIPTION_REVIEW_LOG_BYTES = 1_000_000L
    private const val LOG_CHUNK_SIZE = 3_000
    private const val JSON_INDENT_SPACES = 2
}
