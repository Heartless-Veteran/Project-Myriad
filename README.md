# Project Myriad
## The Definitive Manga and Anime Platform

> **🚀 Migration in Progress**: Project Myriad is currently undergoing a comprehensive migration from React Native to native Android with Kotlin + Jetpack Compose. See [Android Migration Guide](docs/ANDROID_MIGRATION.md) for details.

**Project Myriad** is a comprehensive Android application for manga and anime enthusiasts, featuring AI-powered tools, local media management, and seamless online content discovery. Built with modern Android architecture using **Kotlin**, **Jetpack Compose**, and **Clean Architecture** principles.

### 🚀 Features

#### 🏠 The Vault - Local Media Engine
- **Offline-first management** with smart caching
- Support for `.cbz`, `.cbr` manga formats
- Support for `.mp4`, `.mkv`, `.avi` anime formats
- Metadata scraping and organization
- Local library management

#### 🧠 AI Core - Intelligent Features
- **OCR Translation** for manga text
- **Art Style Matching** using computer vision
- **AI-powered Recommendations** based on user preferences
- **Natural Language Search** for intuitive content discovery
- **Metadata Extraction** from cover images

#### 🌐 The Browser - Online Discovery Engine
- Extensible source system for browsing online content
- Integration with popular manga and anime platforms
- Unified search across multiple sources
- Source management and configuration

### 📱 Platform Support
- **Android**: Primary target platform (API 21-34)
- **Architecture**: ARM, ARM64, x86, x86_64 support
- **Performance**: Hermes JavaScript engine enabled

### 🛠️ Technology Stack
- **Kotlin** with strict null safety
- **Jetpack Compose** with Material 3 Design
- **Clean Architecture** (MVVM pattern)
- **Hilt** for dependency injection
- **Room + Flow** for reactive database operations
- **Retrofit + Kotlinx Serialization** for networking
- **Coil** for image loading
- **Navigation Compose** for navigation
- **WorkManager** for background tasks

### 🏗️ Project Structure (Clean Architecture)
```
kotlin/
├── domain/               # Business logic layer
│   ├── entities/         # Core business entities
│   ├── repositories/     # Repository interfaces
│   └── usecases/         # Business use cases
├── data/                 # Data access layer  
│   ├── repositories/     # Repository implementations
│   ├── database/         # Room database
│   └── network/          # API services
└── presentation/         # UI layer
    ├── components/       # Reusable Compose components
    ├── navigation/       # Navigation setup
    ├── theme/            # Material 3 theme
    └── viewmodels/       # State management
```

### 🚀 Getting Started

#### Prerequisites
- **Android Studio** Flamingo or newer
- **Java 17** or newer
- **Android SDK** API 21-34
- React Native CLI
- Java Development Kit (JDK) 11 or higher

#### Installation
```bash
# Clone the repository
git clone https://github.com/Heartless-Veteran/Project-Myriad.git
cd Project-Myriad

# Install dependencies
npm install

# Build Android project
cd android
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

#### Building for Release
```bash
# Build release APK
cd android
./gradlew assembleRelease

# The APK will be generated in:
# android/app/build/outputs/apk/release/app-release.apk
```

### 🧪 Testing
```bash
# Run unit tests
cd android
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### 📦 Key Dependencies
- **Jetpack Compose BOM**: UI toolkit
- **Material 3**: Design system components
- **Hilt**: Dependency injection framework
- **Room**: Database with Flow support
- **Retrofit**: HTTP client with Kotlinx Serialization
- **Coil**: Image loading library
- **Navigation Compose**: Declarative navigation
- **WorkManager**: Background task management

### 🔧 Configuration
The project uses:
- **Kotlin 1.9.22** with strict null safety
- **Gradle** with Version Catalogs
- **ktlint** for code formatting
- **Detekt** for static analysis
- **JUnit 5** for unit testing
- **Espresso** for UI testing

### 📋 Documentation
- [Android Migration Guide](docs/ANDROID_MIGRATION.md) 
- [Development Guide](docs/DEVELOPMENT.md)
- [Dependency Management](docs/dependency-management.md)

### 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### 🤝 Contributing
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### 📞 Support
For support and questions, please open an issue on GitHub.

---

**Project Myriad** - Bringing manga and anime content together with the power of AI and modern Android technology.

### 🔄 Migration Status
- ✅ **Foundation**: Kotlin + Jetpack Compose architecture
- ✅ **Domain Layer**: Clean Architecture entities and repositories
- ✅ **UI Layer**: Material 3 theme and Navigation Compose
- 🚧 **Data Layer**: Room database implementation (in progress)
- 📋 **Features**: Vault, AI Core, and Browser migration (planned)

See the [Android Migration Guide](docs/ANDROID_MIGRATION.md) for comprehensive migration details and progress tracking.
