package com.heartlessveteran.myriad.di

import com.heartlessveteran.myriad.data.network.MangaDxApi
import com.heartlessveteran.myriad.data.repository.MangaDxSourceRepositoryImpl
import com.heartlessveteran.myriad.domain.repository.SourceRepository
import com.heartlessveteran.myriad.domain.usecase.GetLatestMangaUseCase
import com.heartlessveteran.myriad.domain.usecase.SearchMangaUseCase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Manual dependency injection container for Browse feature
 * (Temporary solution until Hilt is fully enabled)
 */
object BrowseDiContainer {
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (com.heartlessveteran.myriad.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(MangaDxApi.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    
    val mangaDxApi: MangaDxApi = retrofit.create(MangaDxApi::class.java)
    
    val sourceRepository: SourceRepository = MangaDxSourceRepositoryImpl(mangaDxApi)
    
    val getLatestMangaUseCase: GetLatestMangaUseCase = GetLatestMangaUseCase(sourceRepository)
    
    val searchMangaUseCase: SearchMangaUseCase = SearchMangaUseCase(sourceRepository)
}