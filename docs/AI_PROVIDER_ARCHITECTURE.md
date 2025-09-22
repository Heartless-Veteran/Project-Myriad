# AI Provider Architecture

The Project Myriad AI Provider architecture allows users to choose between different AI models (Gemini, OpenAI, etc.) for various AI-powered features including recommendations, art style analysis, and OCR translation.

## Architecture Overview

### Domain Layer (`core/domain/ai/`)

**AIProvider Interface**
```kotlin
interface AIProvider {
    val name: String
    val isAvailable: Boolean
    
    suspend fun generateResponse(prompt: String): Result<String>
    suspend fun generateRecommendations(userPreferences: Map<String, Any>, readingHistory: List<String>): Result<List<String>>
    suspend fun analyzeArtStyle(imageData: ByteArray): Result<List<String>>
    suspend fun translateText(imageData: ByteArray): Result<String>
}
```

### Data Layer (`core/data/ai/`)

**Concrete Implementations:**
- `GeminiProvider` - Google Gemini AI integration
- `OpenAIProvider` - OpenAI GPT integration
- `AIProviderRegistry` - Manages available providers

**Provider Registry:**
```kotlin
class AIProviderRegistry(
    private val geminiProvider: GeminiProvider,
    private val openAIProvider: OpenAIProvider
) {
    fun getProviders(): List<AIProvider>
    fun getProviderByName(name: String): AIProvider?
    fun getDefaultProvider(): AIProvider
}
```

### Feature Layer (`feature/ai/`)

**ViewModel:**
```kotlin
class AIViewModel(private val aiProviderRegistry: AIProviderRegistry) : ViewModel() {
    val currentProvider: StateFlow<AIProvider>
    val availableProviders: StateFlow<List<AIProvider>>
    val aiOperationState: StateFlow<AIOperationState>
    
    fun selectProvider(provider: AIProvider)
    fun generateRecommendations(userPreferences: Map<String, Any>)
    fun analyzeArtStyle(imageData: ByteArray)
    fun translateText(imageData: ByteArray)
}
```

**UI Components:**
- `AIProviderSelector` - Dropdown for provider selection
- `AIProviderDemoScreen` - Complete demo implementation

## Usage Examples

### 1. Basic Provider Selection

```kotlin
@Composable
fun MyScreen(viewModel: AIViewModel) {
    val currentProvider by viewModel.currentProvider.collectAsState()
    val availableProviders by viewModel.availableProviders.collectAsState()
    
    AIProviderSelector(
        providers = availableProviders,
        selectedProvider = currentProvider,
        onProviderSelected = { viewModel.selectProvider(it) }
    )
}
```

### 2. Generate Recommendations

```kotlin
// In your ViewModel or composable
val userPreferences = mapOf(
    "genres" to listOf("action", "adventure"),
    "art_style" to "modern",
    "content_type" to "manga"
)
viewModel.generateRecommendations(userPreferences)
```

### 3. Background Processing with Provider Selection

```kotlin
// Updated BackgroundAIProcessor supports provider selection
backgroundAIProcessor.processOCRTranslation(
    imageData = imageBytes,
    providerName = "OpenAI", // Optional - uses default if not specified
    onComplete = { translatedText -> /* handle result */ },
    onError = { error -> /* handle error */ }
)
```

## Integration with Existing Systems

### Dependency Injection

The AI providers are integrated with the existing manual DI system:

```kotlin
// In DIContainer.kt
val geminiProvider: GeminiProvider by lazy { GeminiProvider() }
val openAIProvider: OpenAIProvider by lazy { OpenAIProvider() }
val aiProviderRegistry: AIProviderRegistry by lazy { 
    AIProviderRegistry(geminiProvider, openAIProvider) 
}
val backgroundAIProcessor: BackgroundAIProcessor by lazy { 
    BackgroundAIProcessor(aiProviderRegistry) 
}
```

### ViewModelFactory

```kotlin
// In ViewModelFactory.kt
AIViewModel::class.java -> {
    AIViewModel(aiProviderRegistry = diContainer.aiProviderRegistry) as T
}
```

## Adding New AI Providers

1. **Create Provider Implementation:**
```kotlin
class ClaudeProvider : AIProvider {
    override val name = "Claude"
    override val isAvailable = true
    
    override suspend fun generateResponse(prompt: String): Result<String> {
        // Implement Claude API integration
    }
    // ... implement other methods
}
```

2. **Register in AIProviderRegistry:**
```kotlin
class AIProviderRegistry(
    private val geminiProvider: GeminiProvider,
    private val openAIProvider: OpenAIProvider,
    private val claudeProvider: ClaudeProvider // Add new provider
) {
    private val providers = listOf(geminiProvider, openAIProvider, claudeProvider)
    // ...
}
```

3. **Update DIContainer:**
```kotlin
val claudeProvider: ClaudeProvider by lazy { ClaudeProvider() }
val aiProviderRegistry: AIProviderRegistry by lazy { 
    AIProviderRegistry(geminiProvider, openAIProvider, claudeProvider) 
}
```

## Error Handling

All AI operations use the existing `Result<T>` pattern:

```kotlin
when (result) {
    is Result.Success -> {
        // Handle successful result
        val data = result.data
    }
    is Result.Error -> {
        // Handle error
        val error = result.exception
        val message = result.message
    }
    is Result.Loading -> {
        // Show loading state
    }
}
```

## Testing

### Unit Tests

- `AIProviderTest` - Tests the AIProvider interface
- `AIProviderRegistryTest` - Tests provider management

### Running Tests

```bash
./gradlew :core:domain:testDebugUnitTest :core:data:testDebugUnitTest --tests "*AI*"
```

## Future Enhancements

1. **Persistent Provider Selection** - Save user's preferred provider in DataStore
2. **Provider Configuration** - Allow API key configuration per provider
3. **Smart Provider Selection** - Auto-select optimal provider based on task type
4. **Usage Analytics** - Track provider performance and user preferences
5. **Offline Providers** - Support for local AI models

## Real API Integration

The current implementations use mock responses. To integrate with real APIs:

1. **Add network dependencies** to respective modules
2. **Implement actual API calls** in provider implementations
3. **Add API key management** through secure storage
4. **Implement proper error handling** for network issues
5. **Add request caching** for performance optimization

## Example Usage in Settings

```kotlin
@Composable
fun AISettingsScreen(viewModel: AIViewModel) {
    Column {
        Text("AI Provider Settings")
        
        AIProviderSelector(
            providers = viewModel.availableProviders.collectAsState().value,
            selectedProvider = viewModel.currentProvider.collectAsState().value,
            onProviderSelected = { viewModel.selectProvider(it) }
        )
        
        // Additional AI settings...
    }
}
```

This architecture provides a flexible, extensible foundation for AI integration that can grow with the project's needs while maintaining clean separation of concerns and following established patterns.