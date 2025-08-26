package com.heartlessveteran.myriad.network

import com.heartlessveteran.myriad.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that adds the Authorization header with Gemini API key
 * for requests to the Google Gemini API.
 */
class GeminiAuthInterceptor : Interceptor {

    init {
        require(BuildConfig.GEMINI_API_KEY.isNotBlank()) {
            "Gemini API key is missing. Please add it to your local.properties file."
        }
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Add Authorization Bearer header with Gemini API key
        val newRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer ${BuildConfig.GEMINI_API_KEY}")
            .build()
            
        return chain.proceed(newRequest)
    }
}