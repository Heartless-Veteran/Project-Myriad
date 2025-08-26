package com.heartlessveteran.myriad.di

import com.heartlessveteran.myriad.network.GeminiAuthInterceptor
import com.heartlessveteran.myriad.network.GeminiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module for providing network-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    /**
     * Qualifier annotation for Gemini-specific OkHttpClient.
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class GeminiClient
    
    /**
     * Qualifier annotation for Gemini-specific Retrofit instance.
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class GeminiRetrofit
    
    /**
     * Provides a JSON serializer instance configured for API communication.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }
    
    /**
     * Provides an HTTP logging interceptor for debugging network requests.
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (com.heartlessveteran.myriad.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    }
    
    /**
     * Provides the Gemini authentication interceptor.
     */
    @Provides
    @Singleton
    fun provideGeminiAuthInterceptor(): GeminiAuthInterceptor {
        return GeminiAuthInterceptor()
    }
    
    /**
     * Provides an OkHttpClient configured specifically for Gemini API calls.
     */
    @Provides
    @Singleton
    @GeminiClient
    fun provideGeminiOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        geminiAuthInterceptor: GeminiAuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(geminiAuthInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Provides a Retrofit instance configured for Gemini API with Kotlinx Serialization.
     */
    @Provides
    @Singleton
    @GeminiRetrofit
    fun provideGeminiRetrofit(
        @GeminiClient okHttpClient: OkHttpClient,
            .baseUrl(GEMINI_BASE_URL)
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
    
    /**
     * Provides the GeminiService as a singleton.
     */
    @Provides
    @Singleton
    fun provideGeminiService(@GeminiRetrofit retrofit: Retrofit): GeminiService {
        return retrofit.create(GeminiService::class.java)
    }
}