# Gemini AI Integration Guide

This guide covers the integration and usage of Google's Gemini AI services within Project Myriad for OCR translation, content analysis, and intelligent features.

## Overview

Project Myriad integrates with Google's Gemini AI to provide:

- **OCR Translation** - Real-time manga page text recognition and translation
- **Art Style Analysis** - Visual categorization and style matching
- **Content Recognition** - Scene detection and character identification  
- **Smart Recommendations** - AI-driven content suggestions
- **Code Analysis** - Automated code quality improvements (development)

## API Configuration

### Prerequisites

**Requirements:**
- Google Cloud Platform account
- Gemini API access enabled
- Valid API key with appropriate quotas
- Internet connectivity for API calls

**Supported Models:**
- **Gemini 1.5 Flash** - Fast, efficient for routine tasks
- **Gemini 1.5 Pro** - Advanced analysis for complex tasks
- **Gemini 1.0 Pro** - Fallback for compatibility

### API Key Setup

**Local Development:**

1. Create `local.properties` in project root:
```properties
geminiApiKey=your-actual-api-key-here
```

2. Configure environment variable:
```bash
export GEMINI_API_KEY="your-actual-api-key-here"
```

**Production Setup:**

1. Use Android Keystore for secure storage
2. Configure build variants with different keys
3. Implement key rotation strategy

### Build Configuration

**Gradle Configuration (`app/build.gradle.kts`):**
```kotlin
android {
    defaultConfig {
        // Load API key from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        
        buildConfigField(
            "String", 
            "GEMINI_API_KEY", 
            "\"${localProperties.getProperty("geminiApiKey", "")}\""
        )
    }
}
```

**Security Considerations:**
- Never commit API keys to version control
- Use different keys for development/production
- Implement quota monitoring and rate limiting
- Enable API key restrictions in Google Cloud Console

## Core AI Features

### OCR Translation

**Purpose:** Extract and translate text from manga pages in real-time.

**Implementation:**
```kotlin
class OCRTranslationService {
    private val geminiClient = GeminiAPIClient()
    
    suspend fun translateMangaPage(
        imageBitmap: Bitmap,
        targetLanguage: String = "en"
    ): TranslationResult {
        return try {
            val ocrResult = geminiClient.performOCR(imageBitmap)
            val translation = geminiClient.translate(
                text = ocrResult.extractedText,
                targetLang = targetLanguage
            )
            TranslationResult.Success(
                originalText = ocrResult.extractedText,
                translatedText = translation.text,
                confidence = ocrResult.confidence,
                textBoxes = ocrResult.boundingBoxes
            )
        } catch (e: Exception) {
            TranslationResult.Error(e.message ?: "Translation failed")
        }
    }
}
```

**Features:**
- **Multi-language Support** - Japanese, Korean, Chinese, and more
- **Text Positioning** - Maintains original text layout and positioning
- **Confidence Scoring** - Quality assessment for translation accuracy
- **Batch Processing** - Efficient handling of multiple pages

**Usage:**
```kotlin
// In your ViewModel or use case
class MangaReaderViewModel : ViewModel() {
    private val ocrService = OCRTranslationService()
    
    fun translateCurrentPage() {
        viewModelScope.launch {
            val result = ocrService.translateMangaPage(
                imageBitmap = currentPageBitmap,
                targetLanguage = userPreferences.translationLanguage
            )
            
            when (result) {
                is TranslationResult.Success -> {
                    _translationState.value = TranslationState.Success(result)
                }
                is TranslationResult.Error -> {
                    _translationState.value = TranslationState.Error(result.message)
                }
            }
        }
    }
}
```

### Art Style Analysis

**Purpose:** Analyze and categorize manga/anime art styles for better organization and recommendations.

**Implementation:**
```kotlin
class ArtStyleAnalyzer {
    private val geminiClient = GeminiAPIClient()
    
    suspend fun analyzeArtStyle(
        coverImage: Bitmap
    ): ArtStyleResult {
        val prompt = """
            Analyze this manga/anime cover image and identify:
            1. Art style category (shounen, shoujo, seinen, josei, etc.)
            2. Visual characteristics (character design, backgrounds, etc.)
            3. Era/period indicators
            4. Similar known series or artists
            5. Target demographic indicators
            
            Provide response in JSON format.
        """.trimIndent()
        
        return geminiClient.analyzeImage(coverImage, prompt)
    }
}
```

**Analysis Categories:**
- **Demographics** - Shounen, Shoujo, Seinen, Josei
- **Genres** - Action, Romance, Horror, Slice of Life
- **Art Periods** - Classic, Modern, Contemporary
- **Style Elements** - Character design, background art, color usage

### Content Recognition

**Purpose:** Identify scenes, characters, and content elements for enhanced search and organization.

**Implementation:**
```kotlin
class ContentRecognitionService {
    private val geminiClient = GeminiAPIClient()
    
    suspend fun recognizeContent(
        pageImage: Bitmap,
        context: MangaContext
    ): ContentRecognition {
        val prompt = """
            Analyze this manga page and identify:
            1. Scene type (action, dialogue, transition, etc.)
            2. Number of characters present
            3. Emotional tone/mood
            4. Important visual elements
            5. Text density and placement
            
            Context: ${context.title}, Chapter ${context.chapter}
        """.trimIndent()
        
        return geminiClient.recognizeContent(pageImage, prompt)
    }
}
```

**Recognition Features:**
- **Scene Classification** - Action, dialogue, transition scenes
- **Character Detection** - Number and positioning of characters
- **Mood Analysis** - Emotional tone and atmosphere
- **Panel Analysis** - Layout and composition understanding

### Smart Recommendations

**Purpose:** Generate personalized content recommendations based on reading patterns and preferences.

**Implementation:**
```kotlin
class RecommendationEngine {
    private val geminiClient = GeminiAPIClient()
    
    suspend fun generateRecommendations(
        userProfile: UserProfile,
        readingHistory: List<ReadingEntry>
    ): RecommendationResult {
        val analysisData = prepareAnalysisData(userProfile, readingHistory)
        
        val prompt = """
            Based on the user's reading history and preferences:
            ${analysisData.toJSON()}
            
            Generate 10 personalized manga/anime recommendations with:
            1. Title and brief description
            2. Reason for recommendation
            3. Similarity score to user preferences
            4. Availability information
        """.trimIndent()
        
        return geminiClient.generateRecommendations(prompt)
    }
}
```

**Recommendation Factors:**
- **Reading History** - Previously read series and ratings
- **Genre Preferences** - Preferred genres and themes
- **Art Style Preferences** - Visual style preferences
- **Reading Patterns** - Reading frequency and completion rates

## API Usage Patterns

### Request Management

**Rate Limiting:**
```kotlin
class GeminiAPIManager {
    private val rateLimiter = RateLimiter.create(10.0) // 10 requests per second
    private val quotaTracker = QuotaTracker()
    
    suspend fun makeRequest(request: GeminiRequest): GeminiResponse {
        rateLimiter.acquire()
        
        if (!quotaTracker.hasQuotaAvailable()) {
            throw QuotaExceededException("Daily quota exceeded")
        }
        
        return try {
            val response = geminiAPI.execute(request)
            quotaTracker.recordUsage(request.estimatedTokens)
            response
        } catch (e: Exception) {
            handleAPIError(e)
            throw e
        }
    }
}
```

**Caching Strategy:**
```kotlin
class GeminiCacheManager {
    private val cache = LruCache<String, CachedResult>(maxSize = 100)
    
    suspend fun getCachedOrFetch(
        request: GeminiRequest
    ): GeminiResponse {
        val cacheKey = request.generateCacheKey()
        
        cache[cacheKey]?.let { cached ->
            if (!cached.isExpired()) {
                return cached.response
            }
        }
        
        val response = apiManager.makeRequest(request)
        cache.put(cacheKey, CachedResult(response, System.currentTimeMillis()))
        
        return response
    }
}
```

### Error Handling

**Graceful Degradation:**
```kotlin
class AIFeatureManager {
    suspend fun performAITask(
        task: AITask,
        fallbackStrategy: FallbackStrategy = FallbackStrategy.CACHE
    ): AIResult {
        return try {
            geminiService.performTask(task)
        } catch (e: NetworkException) {
            handleNetworkError(e, fallbackStrategy)
        } catch (e: QuotaException) {
            handleQuotaError(e, fallbackStrategy)
        } catch (e: APIException) {
            handleAPIError(e, fallbackStrategy)
        }
    }
    
    private suspend fun handleNetworkError(
        error: NetworkException,
        fallback: FallbackStrategy
    ): AIResult {
        return when (fallback) {
            FallbackStrategy.CACHE -> getCachedResult()
            FallbackStrategy.OFFLINE -> performOfflineAlternative()
            FallbackStrategy.FAIL -> AIResult.Error(error.message)
        }
    }
}
```

## Security and Privacy

### Data Protection

**API Request Security:**
- All requests use HTTPS/TLS encryption
- API keys are stored securely using Android Keystore
- Request payload validation and sanitization
- Response data validation before processing

**Privacy Considerations:**
- User content is only sent to Gemini with explicit consent
- No persistent storage of user content on Google servers
- Local processing preferred when possible
- Clear user controls for AI feature usage

**Data Minimization:**
```kotlin
class PrivacyAwareAIService {
    suspend fun processWithPrivacy(
        content: ContentData,
        userConsent: PrivacyConsent
    ): ProcessingResult {
        if (!userConsent.allowsAIProcessing) {
            return ProcessingResult.Skipped("User consent not granted")
        }
        
        val sanitizedContent = sanitizeForAPI(content)
        val result = geminiService.process(sanitizedContent)
        
        // Clear any temporary data
        sanitizedContent.clear()
        
        return result
    }
}
```

### Quota Management

**Usage Monitoring:**
```kotlin
class QuotaManager {
    private val dailyLimit = 1000 // API calls per day
    private val monthlyLimit = 30000 // API calls per month
    
    fun checkQuotaAvailability(): QuotaStatus {
        val dailyUsage = getDailyUsage()
        val monthlyUsage = getMonthlyUsage()
        
        return QuotaStatus(
            dailyRemaining = dailyLimit - dailyUsage,
            monthlyRemaining = monthlyLimit - monthlyUsage,
            hasQuotaAvailable = dailyUsage < dailyLimit && monthlyUsage < monthlyLimit
        )
    }
    
    fun predictUsage(plannedOperations: List<AIOperation>): UsagePrediction {
        val estimatedTokens = plannedOperations.sumOf { it.estimatedTokens }
        val estimatedCost = calculateCost(estimatedTokens)
        
        return UsagePrediction(
            estimatedTokens = estimatedTokens,
            estimatedCost = estimatedCost,
            withinQuota = estimatedTokens <= getRemainingQuota()
        )
    }
}
```

## Testing and Development

### Mock Implementation

**Test Configuration:**
```kotlin
class MockGeminiService : GeminiService {
    private val mockResponses = loadMockResponses()
    
    override suspend fun performOCR(image: Bitmap): OCRResult {
        delay(Random.nextLong(100, 500)) // Simulate network delay
        return mockResponses.getOCRResponse(image.hashCode())
    }
    
    override suspend fun analyzeArtStyle(image: Bitmap): ArtStyleResult {
        delay(Random.nextLong(200, 800))
        return mockResponses.getArtStyleResponse(image.hashCode())
    }
}
```

**Integration Testing:**
```kotlin
@Test
fun testOCRTranslationFlow() = runTest {
    val mockService = MockGeminiService()
    val translator = OCRTranslationService(mockService)
    
    val testImage = loadTestMangaPage()
    val result = translator.translateMangaPage(testImage, "en")
    
    assertTrue(result is TranslationResult.Success)
    result as TranslationResult.Success
    assertNotEmpty(result.translatedText)
    assertTrue(result.confidence > 0.8)
}
```

### Performance Testing

**Benchmarking:**
```kotlin
@Test
fun benchmarkGeminiAPIPerformance() = runTest {
    val service = GeminiAPIService()
    val testImages = loadTestImages(count = 10)
    
    val startTime = System.currentTimeMillis()
    
    testImages.forEach { image ->
        service.performOCR(image)
    }
    
    val totalTime = System.currentTimeMillis() - startTime
    val averageTime = totalTime / testImages.size
    
    assertTrue(averageTime < 2000) // Less than 2 seconds per OCR
}
```

## Troubleshooting

### Common Issues

**API Key Problems:**
```bash
# Verify API key configuration
./gradlew assembleDebug -PgeminiApiKey=test

# Test API connectivity
node scripts/validate-gemini-api.js
```

**Quota Exceeded:**
```kotlin
// Check quota status
val quotaStatus = quotaManager.checkQuotaAvailability()
if (!quotaStatus.hasQuotaAvailable) {
    // Implement fallback strategy
    useOfflineMode()
}
```

**Network Issues:**
```kotlin
// Implement retry with exponential backoff
suspend fun retryWithBackoff(
    maxRetries: Int = 3,
    operation: suspend () -> APIResponse
): APIResponse {
    repeat(maxRetries) { attempt ->
        try {
            return operation()
        } catch (e: NetworkException) {
            if (attempt == maxRetries - 1) throw e
            delay(2.0.pow(attempt).toLong() * 1000)
        }
    }
    throw Exception("Max retries exceeded")
}
```

### Performance Optimization

**Image Preprocessing:**
```kotlin
fun optimizeImageForAPI(bitmap: Bitmap): Bitmap {
    val maxDimension = 1024
    val ratio = min(
        maxDimension.toFloat() / bitmap.width,
        maxDimension.toFloat() / bitmap.height
    )
    
    return if (ratio < 1.0) {
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )
    } else {
        bitmap
    }
}
```

**Batch Processing:**
```kotlin
suspend fun processBatch(
    images: List<Bitmap>,
    batchSize: Int = 5
): List<ProcessingResult> {
    return images.chunked(batchSize).map { batch ->
        batch.map { image ->
            async { processImage(image) }
        }.awaitAll()
    }.flatten()
}
```

## Cost Management

### Usage Optimization

**Smart Processing:**
- Cache results to avoid duplicate API calls
- Use appropriate model for task complexity
- Implement intelligent batching strategies
- Process only when necessary (user-initiated)

**Model Selection:**
```kotlin
fun selectOptimalModel(task: AITask): GeminiModel {
    return when {
        task.complexity == Complexity.LOW -> GeminiModel.FLASH_1_5
        task.requiresHighAccuracy -> GeminiModel.PRO_1_5
        task.isExperimental -> GeminiModel.PRO_1_0
        else -> GeminiModel.FLASH_1_5
    }
}
```

### Budget Controls

**Usage Limits:**
```kotlin
class BudgetManager {
    private val dailyBudget = 10.0 // USD
    private val monthlyBudget = 200.0 // USD
    
    fun checkBudgetAvailability(): BudgetStatus {
        val dailySpend = getCurrentDailySpend()
        val monthlySpend = getCurrentMonthlySpend()
        
        return BudgetStatus(
            canMakeRequest = dailySpend < dailyBudget && monthlySpend < monthlyBudget,
            dailyRemaining = dailyBudget - dailySpend,
            monthlyRemaining = monthlyBudget - monthlySpend
        )
    }
}
```

---

## Related Documentation

- [Requirements Specification](requirements.md) - Technical requirements
- [Automated Workflow](AUTOMATED_WORKFLOW.md) - CI/CD integration
- [Development Guide](../DEVELOPMENT.md) - Setup instructions
- [Security Policy](../SECURITY.md) - Security guidelines

---

*This integration guide ensures safe, efficient, and cost-effective use of Gemini AI services within Project Myriad.*

*For API issues and support, refer to the Google Cloud documentation and support channels.*

*Last updated: December 2024*