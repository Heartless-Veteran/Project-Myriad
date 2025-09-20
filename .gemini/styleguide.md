# Project Myriad Development Style Guide

This document outlines the coding conventions and best practices for the Project Myriad Android application. It is based on the project's existing development guidelines.

## General Principles

- **Readability**: Code should be easy to understand for all team members.
- **Maintainability**: Code should be easy to modify and extend.
- **Consistency**: Adhering to a consistent style across all projects improves collaboration and reduces errors.
- **Performance**: While readability is paramount, code should be efficient.

## File Organization

- Organize files by feature or domain following Android best practices.
- Keep related files close to each other.
- Use consistent naming conventions:
  - Classes: `PascalCase.kt` (e.g., `MangaRepository.kt`)
  - Functions: `camelCase` (e.g., `fetchMangaById`)
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `lowercase.separated.by.dots`

## Kotlin Guidelines

- **Classes and Functions**:
  - Use `class` for defining objects.
  - Use `data class` for simple data holders.
  - Use `sealed class` for representing restricted class hierarchies.
  - Use `interface` for defining contracts.
- **Type Safety**:
  - Prefer nullable types over throwing exceptions when appropriate.
  - Use `lateinit` only when necessary and document why.
  - Use type inference when the type is obvious, explicit types when clarity is needed.

**Example of a well-structured repository:**
```kotlin
/**
 * Repository for managing manga data operations.
 * Handles both local database and remote API interactions.
 */
class MangaRepository @Inject constructor(
    private val localDataSource: MangaLocalDataSource,
    private val remoteDataSource: MangaRemoteDataSource
) {
    suspend fun getMangaById(id: String): Result<Manga> {
        // ... implementation
    }
}
```

## Android Architecture Guidelines

- **Component Structure**:
  - Use MVVM architecture with ViewModels and Repository pattern.
  - Use Jetpack Compose for pure declarative UI (no Activities/Fragments for UI)
  - Keep Activities lightweight as entry points, delegate business logic to ViewModels.
  - Use dependency injection with Hilt for better testability.
- **State Management**:
  - Use StateFlow for reactive UI updates.
  - Use sealed classes for representing different UI states.
  - Handle configuration changes properly with ViewModels.
- **Performance**:
  - Use appropriate lifecycle-aware components.
  - Implement proper memory management and avoid memory leaks.
  - Use RecyclerView for long lists of data.
- **UI Guidelines**:
  - Follow Material Design principles.
  - Use Jetpack Compose for modern UI development.
  - Implement proper accessibility features.

## Naming Conventions

- **Variables and Functions**: `camelCase`
- **Constants**: `UPPER_SNAKE_CASE`
- **Classes**: `PascalCase`
- **Packages**: `lowercase.separated.by.dots`
- **Files**: `PascalCase.kt` for classes, `camelCase.kt` for utilities

## Code Style and Formatting

- **Line Length**: Maximum 120 characters.
- **Indentation**: 4 spaces (Android Studio default).
- **String Templates**: Use string templates (`"Hello $name"`) instead of concatenation.
- **Trailing Commas**: Use trailing commas for multi-line parameter lists and collections.

## Documentation and Comments

- Write clear, self-documenting code.
- Use KDoc comments for all public functions, classes, and interfaces.
- Explain the "why", not just the "what" in comments for complex or non-obvious code.

**KDoc Example:**
```kotlin
/**
 * Truncates a string to a specified length and appends an ellipsis.
 *
 * @param text The string to truncate.
 * @param maxLength The maximum length of the string.
 * @return The truncated string.
 */
fun truncateText(text: String, maxLength: Int): String {
    // ... implementation
}
```

## Testing

- Write unit tests for all business logic, repositories, and use cases.
- Write UI tests for critical user flows using Espresso or Compose Testing.
- Mock all external dependencies using MockK.
- Aim for high test coverage of core application logic.
- Use Test Driven Development (TDD) when appropriate.

By following these guidelines, Gemini Code Assist can help maintain the quality and consistency of the Project Myriad Android codebase.
