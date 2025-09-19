package com.heartlessveteran.myriad.network

import com.heartlessveteran.myriad.BuildConfig
import okhttp3.Request
import okio.Timeout
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit test for GeminiAuthInterceptor to ensure it correctly adds
 * the Authorization header with the API key.
 */
class GeminiAuthInterceptorTest {
    
    @Test
    fun `interceptor adds authorization header`() {
        val interceptor = GeminiAuthInterceptor()
        
        // Create a mock chain that captures the request
        var capturedRequest: Request? = null
        val mockChain = object : okhttp3.Interceptor.Chain {
            override fun request(): Request = Request.Builder()
                .url("https://example.com/test")
                .build()
            
            override fun proceed(request: Request): okhttp3.Response {
                capturedRequest = request
                // Return a minimal mock response
                return okhttp3.Response.Builder()
                    .request(request)
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(okhttp3.ResponseBody.create(null, ""))
                    .build()
            }
            
            override fun connection(): okhttp3.Connection? = null
            override fun call(): okhttp3.Call = object : okhttp3.Call {
                override fun request(): Request = mockChain.request()
                override fun execute(): okhttp3.Response {
                    throw UnsupportedOperationException("Not implemented in mock")
                }
                override fun enqueue(responseCallback: okhttp3.Callback) {
                    throw UnsupportedOperationException("Not implemented in mock")
                }
                override fun cancel() {}
                override fun isExecuted(): Boolean = false
                override fun isCanceled(): Boolean = false
                override fun clone(): okhttp3.Call = this
                override fun timeout(): Timeout = Timeout.NONE
            }
            override fun connectTimeoutMillis(): Int = 30000
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): okhttp3.Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 30000
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): okhttp3.Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 30000
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): okhttp3.Interceptor.Chain = this
        }
        
        // Execute the interceptor
        interceptor.intercept(mockChain)
        
        // Verify the authorization header was added
        assertNotNull("Request should have been captured", capturedRequest)
        val authHeader = capturedRequest?.header("Authorization")
        assertNotNull("Authorization header should be present", authHeader)
        assertTrue("Authorization header should start with 'Bearer '", 
            authHeader?.startsWith("Bearer") == true)
        
        // Note: In unit tests, BuildConfig.GEMINI_API_KEY might be empty or a test value
        // The important thing is that the header is present and formatted correctly
    }
}