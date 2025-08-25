# Project Myriad Development Guidelines

This document provides essential information for developers working on Project Myriad, a native Android application built with Kotlin and Jetpack Compose for manga and anime content. Following these guidelines ensures consistency, maintainability, and quality across the codebase.

## Build and Configuration Instructions

### Prerequisites

- Android Studio (latest stable version)
- JDK 17 or higher
- Kotlin 1.9+
- Android SDK (API 21-34)

### Setting Up the Development Environment

1. Clone the repository
2. Open the project in Android Studio
3. Let Gradle sync and download dependencies
4. Connect an Android device or start an emulator

### Running the Application

#### Debug Build

```bash
# From Android Studio: Click the 'Run' button or use Shift+F10
# From command line:
./gradlew assembleDebug
./gradlew installDebug
```

#### Release Build

```bash
# Generate a release build
./gradlew assembleRelease
```

The APK will be generated at `app/build/outputs/apk/release/app-release.apk`.

### Configuration Files

- **build.gradle (Project)**: Project-level build configuration and dependencies.
- **build.gradle (Module: app)**: App-level build configuration, dependencies, and Android settings.
- **gradle.properties**: Gradle configuration and build optimization settings.
- **proguard-rules.pro**: Code obfuscation and optimization rules for release builds.

## Testing Information

### Test Framework

Project Myriad uses JUnit 5, AndroidX Test, and Turbine for testing. The test configuration is defined in:
- `app/build.gradle`: Test dependencies and configuration
- Test source sets: `src/test/` (unit tests) and `src/androidTest/` (instrumented tests)

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a specific test class
./gradlew test --tests="com.projectmyriad.domain.usecase.GetMangaUseCaseTest"

# Run tests with coverage report
./gradlew testDebugUnitTestCoverage
```

### Writing Tests

Tests should be placed in appropriate directories:
- Unit tests: `src/test/java/`
- Instrumented tests: `src/androidTest/java/`

#### ViewModel Test Example

```kotlin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class LibraryViewModelTest {
    
    @Test
    fun `when loadManga is called, then manga list is updated`() = runTest {
        // Given
        val repository = FakeMangaRepository()
        val viewModel = LibraryViewModel(repository)
        
        // When
        viewModel.loadManga()
        
        // Then
        assertTrue(viewModel.uiState.value.manga.isNotEmpty())
    }
}
```

#### UseCase Test Example

```kotlin
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class GetMangaUseCaseTest {
    
    @Test
    fun `when execute is called, then returns manga from repository`() = runTest {
        // Given
        val repository = FakeMangaRepository()
        val useCase = GetMangaUseCase(repository)
        
        // When
        val result = useCase.execute()
        
        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
}
```

### Mocking Dependencies

For testing with dependencies, use MockK or create fake implementations:

```kotlin
// Mock a repository using MockK
@Test
fun `test with mocked repository`() {
    // Given
    val mockRepository = mockk<MangaRepository>()
    every { mockRepository.getManga() } returns flowOf(listOf(testManga))
    
    // When
    val useCase = GetMangaUseCase(mockRepository)
    val result = useCase.execute()
    
    // Then
    verify { mockRepository.getManga() }
}

// Fake implementation for testing
class FakeMangaRepository : MangaRepository {
    private val manga = mutableListOf<Manga>()
    
    override fun getManga(): Flow<List<Manga>> = flowOf(manga.toList())
    override suspend fun addManga(manga: Manga) {
        this.manga.add(manga)
    }
}
```

## Kotlin Guidelines

### Data Classes and Types

- Use data classes for simple data containers
- Use sealed classes for representing restricted hierarchies
- Use interfaces for defining contracts
- Place shared models in the `domain/model` package

```kotlin
// Good
data class UserProfile(
    val id: String,
    val username: String,
    val preferences: UserPreferences
)

// Good
sealed class ContentFormat {
    object Manga : ContentFormat()
    object Anime : ContentFormat()
}
```

### Type Safety

- Leverage Kotlin's null safety features
- Use generics for reusable classes and functions
- Add explicit return types to public functions
- Prefer immutable data structures when possible

```kotlin
// Good
suspend fun <T : ContentItem> fetchContent(id: String): Result<T> {
    // Implementation
}

// Avoid
fun processData(data: Any?): Any? {
    // Implementation
}
```

## Android Architecture Guidelines

### Component Structure

- Use ViewModels for UI-related data and business logic
- Keep Activities/Fragments lightweight - delegate to ViewModels
- Organize files with the following structure:
  1. Package declaration and imports
  2. Constants
  3. Class definition
  4. Public methods
  5. Private methods
- Keep classes focused on a single responsibility
- Extract reusable logic into use cases

```kotlin
class LibraryViewModel @Inject constructor(
    private val getMangaUseCase: GetMangaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun loadManga() {
        viewModelScope.launch {
            getMangaUseCase.execute()
                .collect { manga ->
                    _uiState.update { it.copy(manga = manga) }
                }
        }
    }
}
```

### State Management

- Use StateFlow and SharedFlow for reactive programming
- Keep ViewModels lifecycle-aware and avoid memory leaks
- Use Hilt for dependency injection
- Follow MVVM pattern with clear separation of concerns:
  - `View`: Compose UI components
  - `ViewModel`: UI state and business logic coordination
  - `Model`: Data layer (repositories, use cases)

### Performance Optimization

- Use LazyColumn/LazyRow for lists with many items
- Leverage Compose's built-in optimizations (remember, derivedStateOf)
- Use Coil for efficient image loading and caching
- Implement proper database indexing for Room queries
- Use Flow for reactive data streams

## File Organization

### Directory Structure

- Organize files by layer and feature rather than by file type
- Keep related files close to each other
- Use consistent naming conventions

```
src/main/java/com/projectmyriad/
├── data/
│   ├── local/          # Room database, DAOs, local data sources
│   ├── remote/         # Retrofit services, API interfaces
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Data models and entities
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic use cases
├── ui/
│   ├── components/     # Reusable Compose components
│   ├── navigation/     # Navigation graphs and destinations
│   ├── screens/        # Feature screens (library, reader, settings)
│   └── theme/          # Material 3 theme, colors, typography
└── di/                # Hilt dependency injection modules
```

### Naming Conventions

- Use PascalCase for class names and interfaces
- Use camelCase for functions, variables, and package names
- Use SCREAMING_SNAKE_CASE for constants
- Add descriptive suffixes to files:
  - `ViewModel` for ViewModels
  - `Repository` for repositories
  - `UseCase` for use cases
  - `Dao` for data access objects

## Code Style

### Formatting

- Use consistent indentation (4 spaces)
- Limit line length to 120 characters
- Follow Kotlin coding conventions
- Use ktlint for automatic code formatting
- Import organization: Android -> Third party -> Project

### Comments and Documentation

- Write self-documenting code with clear class and function names
- Add KDoc comments for public APIs and complex functions
- Include comments for non-obvious code sections
- Document data classes and interfaces with descriptive comments

```kotlin
/**
 * Fetches content from the specified source and applies filters
 * @param source The content source identifier
 * @param filters Optional filters to apply to the results
 * @return A flow of content items matching the criteria
 */
suspend fun fetchContentFromSource(
    source: String, 
    filters: ContentFilters? = null
): Flow<List<ContentItem>> {
    // Implementation
}
```

## Testing Guidelines

- Write unit tests for ViewModels, use cases, and repositories
- Write UI tests for Compose screens and components
- Use integration tests for critical user flows
- Mock external dependencies using MockK or fakes
- Aim for high test coverage of domain layer

## Accessibility Guidelines

- Use semantic Compose components with proper semantics
- Add contentDescription for images and icons
- Ensure sufficient color contrast (4.5:1 minimum)
- Support TalkBack screen reader
- Test with Android accessibility scanner

## Performance Guidelines

- Use LazyColumn/LazyRow for large lists
- Optimize Compose recomposition with remember and key
- Minimize database queries with proper caching
- Use Coil for efficient image loading
- Profile and optimize slow operations

By following these guidelines, we ensure that Project Myriad maintains a high standard of code quality, performance, and user experience.