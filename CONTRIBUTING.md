# Contributing to Project Myriad

Welcome to **Project Myriad** - The Definitive Manga and Anime Platform! We're excited to have you contribute to this comprehensive Kotlin Android application that brings together AI-powered tools, local media management, and online content discovery.

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Jellyfish (2023.3.1) or later
- **JDK 17** or higher
- **Android SDK** with API levels 24-36
- **Git** for version control
- **Kotlin 2.2.20** experience recommended

### Development Environment Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Heartless-Veteran/Project-Myriad.git
   cd Project-Myriad
   ```

2. **Configure API Keys**
   ```bash
   # Copy the example local properties file
   cp local.properties.example local.properties
   ```

3. **Build the Project**
   ```bash
   ./gradlew build
   ```
   
   > **Note**: First build may take 2-5 minutes for Kotlin compilation. This is expected for the comprehensive build system with quality checks.

4. **Run Tests**
   ```bash
   ./gradlew test
   ```

5. **Run Quality Checks**
   ```bash
   # Run all quality checks
   ./gradlew check
   
   # Run specific checks
   ./gradlew ktlintCheck  # Code formatting
   ./gradlew detekt       # Static analysis
   ./gradlew lint         # Android lint
   ```

## 🏗️ Architecture Overview

Project Myriad follows **Clean Architecture** principles with **MVVM** pattern:

```
app/
├── data/                   # Data layer
│   ├── database/          # Room database entities and DAOs
│   ├── repository/        # Repository implementations
│   ├── cache/             # Memory caching system
│   └── validation/        # Data validation logic
├── domain/                # Business logic layer
│   ├── model/            # Domain models
│   ├── repository/       # Repository interfaces
│   └── usecase/          # Use cases
├── network/              # Network layer (Retrofit, API services)
├── ui/                   # Presentation layer
│   ├── screens/          # Jetpack Compose screens
│   ├── viewmodel/        # ViewModels
│   ├── theme/           # Material 3 theming
│   └── common/          # Reusable UI components
├── navigation/           # Navigation logic
└── di/                  # Dependency injection (Hilt)
```

### Key Technologies

- **Kotlin** - Primary language
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design system
- **Hilt** - Dependency injection
- **Room** - Local database
- **Retrofit** - Network operations
- **Coroutines** - Asynchronous programming

## 🛠️ Development Workflow

### Branch Strategy

- `main` - Production-ready code
- `alpha` - Integration branch for features and pre-production testing
- `feature/*` - Individual feature development
- `hotfix/*` - Critical bug fixes

### Workflow Steps

1. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Your Changes**
   - Follow the coding standards (see below)
   - Write tests for new functionality
   - Update documentation as needed

3. **Test Your Changes**
   ```bash
   ./gradlew test
   ./gradlew lint
   ```

4. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

5. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   ```
   Then create a Pull Request on GitHub.

## 📋 Coding Standards

### Kotlin Style Guide

Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html):

- **Naming**: Use camelCase for functions and variables, PascalCase for classes
- **Indentation**: 4 spaces, no tabs
- **Line Length**: 120 characters maximum
- **Imports**: Group and sort imports, remove unused imports

### Code Organization

- **One class per file** (except for small, related data classes)
- **Descriptive naming** - prefer clarity over brevity
- **Function length** - keep functions focused and under 20 lines when possible
- **Comments** - Use KDoc for public APIs, inline comments for complex logic

### Example Code Style

```kotlin
/**
 * Repository for managing manga data with local and remote sources.
 * 
 * @property mangaDao Local database access object
 * @property apiService Remote API service
 */
class MangaRepositoryImpl @Inject constructor(
    private val mangaDao: MangaDao,
    private val apiService: MangaApiService
) : MangaRepository {
    
    override suspend fun getManga(id: String): Result<Manga> {
        return try {
            val localManga = mangaDao.getMangaById(id)
            if (localManga != null) {
                Result.Success(localManga.toDomainModel())
            } else {
                fetchMangaFromRemote(id)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get manga: ${e.message}")
        }
    }
    
    private suspend fun fetchMangaFromRemote(id: String): Result<Manga> {
        // Implementation details...
    }
}
```

### Compose Guidelines

- **Preview functions** for all composables
- **State hoisting** - lift state up when shared between composables
- **Theming** - use Material 3 design tokens
- **Accessibility** - include content descriptions and semantic properties

```kotlin
@Composable
fun MangaCard(
    manga: Manga,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick(manga.id) },
        modifier = modifier.semantics { contentDescription = "Manga: ${manga.title}" }
    ) {
        // Card content...
    }
}

@Preview(showBackground = true)
@Composable
private fun MangaCardPreview() {
    MyriadTheme {
        MangaCard(
            manga = Manga.sample(),
            onClick = {}
        )
    }
}
```

## 🧪 Testing Guidelines

### Testing Strategy

- **Unit Tests** - Business logic, repositories, use cases
- **Integration Tests** - Database operations, API interactions
- **UI Tests** - Critical user flows with Compose Testing

### Test Structure

```kotlin
class MangaRepositoryTest {
    
    @MockK
    private lateinit var mangaDao: MangaDao
    
    @MockK
    private lateinit var apiService: MangaApiService
    
    private lateinit var repository: MangaRepositoryImpl
    
    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        repository = MangaRepositoryImpl(mangaDao, apiService)
    }
    
    @Test
    fun `getManga returns success when manga exists locally`() = runTest {
        // Given
        val expectedManga = MangaEntity.sample()
        coEvery { mangaDao.getMangaById(any()) } returns expectedManga
        
        // When
        val result = repository.getManga("test-id")
        
        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedManga.title, (result as Result.Success).data.title)
    }
}
```

## 🎯 Priority Areas for Contribution

### High Priority

1. **Complete NavigationService** - Implement route parsing for deep linking
2. **File Management System** - Add .cbz/.cbr import/export functionality  
3. **Source Extension System** - Plugin architecture for content providers
4. **Download Manager** - Queue management with pause/resume capabilities

### Medium Priority

1. **UI Improvements** - Enhanced reader modes and customizations
2. **Library Management** - Collections, tagging, and organization
3. **Search Enhancement** - Advanced filtering and discovery features
4. **Performance Optimization** - Memory management and caching strategies

### Documentation

1. **API Documentation** - Complete KDoc coverage
2. **Architecture Guides** - Detailed explanations of design decisions
3. **User Guides** - Feature documentation and tutorials
4. **Troubleshooting** - Common issues and solutions

## 🐛 Bug Reports

When reporting bugs, please include:

- **Android version** and **device model**
- **App version** and **build number**
- **Steps to reproduce** the issue
- **Expected behavior** vs **actual behavior**
- **Screenshots** or **logs** if applicable
- **Crash logs** from logcat if available

Use our **issue templates** to ensure all necessary information is provided.

## ✨ Feature Requests

For new features:

- **Clear description** of the proposed feature
- **Use case** - how would this benefit users?
- **Mockups** or **wireframes** if applicable
- **Technical considerations** - any implementation thoughts
- **Priority level** - how important is this feature?

## 🔍 Code Review Guidelines

### For Authors

- **Self-review** your code before submitting
- **Small, focused PRs** - easier to review and merge
- **Descriptive commit messages** following conventional commits
- **Tests included** for new functionality
- **Documentation updated** as needed

### For Reviewers

- **Be constructive** - suggest improvements, don't just criticize
- **Focus on logic** - not just style (we have automated tools for that)
- **Ask questions** - if something isn't clear, ask for clarification
- **Test the changes** - pull and verify the functionality works
- **Approve promptly** - don't let good PRs sit idle

## 📜 Commit Message Format

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types

- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Formatting, missing semicolons, etc.
- `refactor` - Code changes that neither fix a bug nor add a feature
- `perf` - Performance improvements
- `test` - Adding or updating tests
- `chore` - Maintenance tasks

### Examples

```bash
feat(reader): add webtoon reading mode
fix(database): resolve manga import crash
docs(contributing): update setup instructions
refactor(navigation): simplify route definitions
```

## 🎨 UI/UX Guidelines

### Material 3 Design

- Follow [Material 3 guidelines](https://m3.material.io/)
- Use **semantic color tokens** from our theme
- Ensure **accessibility** with proper contrast ratios
- Support **dark/light themes**

### Manga/Anime Theming

- **Consistent visual language** reflecting manga/anime aesthetics
- **Cultural sensitivity** in design choices
- **Reader-friendly** typography and spacing
- **Immersive experience** without overwhelming the content

## 🚀 Release Process

### Version Numbering

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR** - Breaking changes
- **MINOR** - New features, backwards compatible
- **PATCH** - Bug fixes, backwards compatible

### Release Checklist

- [ ] All tests passing
- [ ] Documentation updated
- [ ] Performance benchmarks run
- [ ] Security scan completed
- [ ] Beta testing conducted
- [ ] Release notes prepared

## 💬 Communication

### Discord Server
Join our community for real-time discussions: [Discord Invite](#)

### GitHub Discussions
Use GitHub Discussions for:
- **General questions** about the project
- **Feature brainstorming** and feedback
- **Show and tell** - share your contributions
- **Community support** - help each other

### Issue Labels

- `good first issue` - Perfect for newcomers
- `help wanted` - We need community assistance
- `bug` - Something isn't working
- `enhancement` - New feature or improvement
- `documentation` - Improvements to docs
- `priority: high/medium/low` - Importance level

## 🏆 Recognition

Contributors are recognized in:
- **README.md** - All contributors listed
- **Release notes** - Major contributions highlighted  
- **Hall of Fame** - Outstanding contributors featured
- **Discord roles** - Special recognition for active members

## 📄 License

By contributing to Project Myriad, you agree that your contributions will be licensed under the [MIT License](LICENSE).

---

## 🤝 Questions?

Don't hesitate to ask questions! We're here to help:

- **Create a GitHub Discussion** for general questions
- **Join our Discord** for real-time chat
- **Comment on issues** for specific clarifications
- **Reach out to maintainers** directly if needed

Thank you for contributing to **Project Myriad**! Together, we're building the ultimate platform for manga and anime enthusiasts. 🚀📚🎌
