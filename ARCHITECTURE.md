# Project Myriad - Architecture Documentation

This document provides comprehensive architecture information for Project Myriad, consolidating all architectural details in one authoritative location.

## Overview

Project Myriad follows **Clean Architecture** principles with MVVM pattern, ensuring separation of concerns, testability, and maintainability throughout the application.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    🎨 Presentation Layer                    │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │   Compose   │ │  ViewModels │ │   Navigation        │   │
│  │   Screens   │ │   (MVVM)    │ │   (Type-safe)       │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                               │
┌─────────────────────────────────────────────────────────────┐
│                   🧠 Domain Layer                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │  Use Cases  │ │ Repositories│ │   Domain Models     │   │
│  │ (Business)  │ │(Interfaces) │ │   (Pure Kotlin)     │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                               │
┌─────────────────────────────────────────────────────────────┐
│                    💾 Data Layer                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │    Room     │ │   Retrofit  │ │   File System       │   │
│  │  Database   │ │  API Client │ │   (.cbz/.cbr)       │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Presentation Layer
- **Jetpack Compose**: Declarative UI toolkit with Material 3 design
- **ViewModels**: MVVM pattern for state management with StateFlow
- **Type-safe Navigation**: Jetpack Navigation Compose for screen transitions

### Domain Layer  
- **Use Cases**: Business logic encapsulation and orchestration
- **Repository Interfaces**: Data access abstractions following dependency inversion
- **Domain Models**: Pure Kotlin entities representing core business concepts

### Data Layer
- **Room Database**: Local persistence for offline-first functionality
- **Retrofit API**: HTTP client for online content discovery
- **File System**: Direct file access for local media management (.cbz/.cbr files)

## Core Architectural Principles

### Single Source of Truth
- **Room database** serves as the authoritative data source for all application state
- All UI state derives from database entities through reactive streams
- Ensures data consistency across the entire application

### Unidirectional Data Flow
- **UI → ViewModel → Use Case → Repository → Data Source**
- Clear, predictable data flow from user interactions to data persistence
- Eliminates circular dependencies and makes debugging straightforward

### Separation of Concerns
- **Each layer has distinct responsibilities** with clear boundaries
- **Domain layer** contains only business logic, no Android dependencies
- **Data layer** handles persistence and external data sources
- **Presentation layer** focuses solely on UI rendering and user interaction

### Dependency Inversion
- **High-level modules don't depend on low-level modules**
- Abstractions (interfaces) define contracts between layers
- Concrete implementations depend on abstractions, not vice versa
- Enables easy testing and flexibility for implementation changes

## Technology Stack

### Core Technologies
- **Kotlin 2.2.20** - Modern, expressive programming language
- **Android SDK 36** - Latest Android platform capabilities
- **Jetpack Compose** - Declarative UI framework with Material 3
- **Coroutines & Flow** - Asynchronous programming and reactive streams

### Architecture Components
- **Clean Architecture** - Domain/Data/Presentation layer separation
- **MVVM Pattern** - ViewModel + StateFlow for state management
- **Dependency Injection** - Manual DI (Hilt planned when KAPT/KSP ready)
- **Room Database** - Local database with type-safe queries

### Data Management
- **Room 2.6.1** - Local database persistence
- **Retrofit 2.9.0** - Type-safe HTTP client for API communication
- **OkHttp 4.12.0** - Efficient network operations
- **Kotlinx Serialization** - JSON parsing and data serialization

## Module Structure

```
app/src/main/kotlin/com/heartlessveteran/myriad/
├── ui/                     # Presentation Layer
│   ├── screens/           # Jetpack Compose screens
│   ├── viewmodel/         # MVVM ViewModels
│   ├── navigation/        # Navigation setup
│   └── theme/            # Material 3 theming
├── domain/               # Domain Layer (Pure Kotlin)
│   ├── entities/         # Core business entities
│   ├── repository/       # Repository interfaces
│   ├── models/          # Domain models (Result, etc.)
│   └── usecases/        # Business logic use cases
├── data/                # Data Layer
│   ├── database/        # Room database implementation
│   ├── repository/      # Repository implementations
│   └── network/         # API services and networking
└── di/                  # Dependency injection setup
```

## Plugin-Based Source System

Project Myriad implements an extensible plugin architecture for manga sources, enabling support for multiple content providers.

### Plugin Architecture Overview

#### Source Interface
All manga sources implement a common `Source` interface:

```kotlin
interface Source {
    val id: String
    val name: String
    val lang: String
    val baseUrl: String
    
    suspend fun getLatestManga(page: Int): Result<List<Manga>>
    suspend fun searchManga(query: String, page: Int): Result<List<Manga>>
    suspend fun getMangaDetails(url: String): Result<Manga>
    suspend fun getChapterPages(url: String): Result<List<String>>
    suspend fun getPopularManga(page: Int): Result<List<Manga>>
    suspend fun getChapterList(manga: Manga): Result<List<MangaChapter>>
}
```

#### Plugin Management
- **Plugin Entity**: Database storage for plugin metadata and state
- **PluginManager**: Manages plugin lifecycle (install, enable/disable, load)
- **PluginRepository**: Data access layer for plugin persistence

#### Global Search System
- **SearchManager**: Aggregates search results across all enabled sources
- **Parallel Processing**: Searches execute concurrently using Kotlin Coroutines
- **Graceful Degradation**: Individual source failures don't break overall search
- **Result Grouping**: Results organized by source for clear presentation

### Error Handling Strategy

The plugin system implements robust error handling:

- **Individual Source Failures**: Isolated failures don't affect other sources
- **Network Timeouts**: Graceful handling with appropriate user feedback
- **Invalid Responses**: Proper error propagation with meaningful messages
- **Plugin State Management**: Database persistence ensures consistent state

### Performance Considerations

- **Parallel Execution**: All sources queried simultaneously using Coroutines
- **Result Streaming**: Uses Flow for reactive UI updates
- **Caching**: Plugin state cached in Room database for quick access
- **Lazy Loading**: Source instances created only when needed

## Implementation Status

For detailed feature implementation status and current development progress, see [ARCHITECTURE_STATUS.md](ARCHITECTURE_STATUS.md).

For implementation-specific details and validation, see [ARCHITECTURE_IMPLEMENTATION.md](ARCHITECTURE_IMPLEMENTATION.md).

## Related Documentation

- **[Development Guide](DEVELOPMENT.md)** - Setup, build instructions, and coding guidelines
- **[Requirements Specification](docs/requirements.md)** - Detailed technical requirements
- **[Contributing Guidelines](CONTRIBUTING.md)** - Development standards and contribution process
- **[Security Policy](SECURITY.md)** - Security guidelines and best practices
- **[Documentation Index](docs/INDEX.md)** - Complete documentation navigation guide

---

*This architecture documentation serves as the authoritative source for all architectural decisions and patterns used in Project Myriad.*

*For questions about architecture or to propose changes, please open an issue in the project repository.*

*Last updated: December 2024*