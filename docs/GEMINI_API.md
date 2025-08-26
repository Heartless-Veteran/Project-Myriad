# Google Gemini API Integration

This document explains how to use the Google Gemini API integration in Project Myriad.

## Setup

1. **Get your API Key**: Visit [Google AI Studio](https://makersuite.google.com/app/apikey) to generate your Gemini API key.

2. **Configure Local Properties**: Create a `local.properties` file in the root directory (or copy from `local.properties.example`):
   ```properties
   # Google Gemini API Key
   geminiApiKey=your_actual_api_key_here
   ```

3. **Build the App**: The API key will be automatically injected into `BuildConfig.GEMINI_API_KEY` during build.

## Usage

### Dependency Injection

The Gemini service is available as a singleton through Hilt dependency injection:

```kotlin
@AndroidEntryPoint
class YourActivity : ComponentActivity() {
    
    @Inject
    lateinit var geminiService: GeminiService
    
    // Use geminiService in your code
}
```

### Making API Calls

```kotlin
class GeminiRepository @Inject constructor(
    private val geminiService: GeminiService
) {
    suspend fun chatWithGemini(message: String): String {
        try {
            val request = GeminiChatRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(message))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 1024
                )
            )
            
            val response = geminiService.generateContent(request)
            return response.candidates.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text 
                ?: "No response"
                
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }
}
```

### In a ViewModel

```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository
) : ViewModel() {
    
    fun sendMessage(message: String) {
        viewModelScope.launch {
            val response = geminiRepository.chatWithGemini(message)
            // Handle response
        }
    }
}
```

## Architecture Components

### GeminiService
- **Interface**: Defines the Retrofit API endpoints
- **Endpoint**: `/v1/models/gemini-pro:generateContent`
- **Method**: POST with JSON body

### Data Classes
- **GeminiChatRequest**: Request structure with contents and generation config
- **GeminiChatResponse**: Response structure with candidates
- **GeminiContent**: Content wrapper with parts and role
- **GeminiPart**: Individual text part
- **GeminiGenerationConfig**: Controls response generation parameters

### GeminiAuthInterceptor
- Automatically adds `Authorization: Bearer <API_KEY>` header
- Uses `BuildConfig.GEMINI_API_KEY` from local.properties

### NetworkModule
- Provides Hilt dependency injection configuration
- Sets up OkHttpClient with authentication and logging
- Configures Retrofit with Kotlinx Serialization
- Exposes GeminiService as singleton

## Security

- ✅ `local.properties` is in `.gitignore` - API keys won't be committed
- ✅ BuildConfig exposes key only to app runtime, not source code
- ✅ HTTPS communication with Google's secure endpoints
- ✅ No API key hardcoding in source files

## Testing

Unit tests are included for:
- **GeminiServiceDataClassTest**: Tests JSON serialization/deserialization
- **GeminiAuthInterceptorTest**: Tests authorization header injection

Run tests with:
```bash
./gradlew testDebugUnitTest --tests "*Gemini*"
```

## Error Handling

The integration includes proper error handling:
- Network timeouts (30s connect, 60s read/write)
- JSON parsing errors with `ignoreUnknownKeys = true`
- Missing API key scenarios
- HTTP error responses

## Rate Limits and Best Practices

- Google Gemini API has rate limits - implement appropriate retry logic
- Use appropriate `maxOutputTokens` to control costs
- Consider caching responses for similar queries
- Monitor API usage through Google Cloud Console