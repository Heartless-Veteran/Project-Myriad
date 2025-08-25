# Project Myriad
## The Definitive Manga and Anime Platform - Kotlin Android Edition

**Project Myriad** is a comprehensive Kotlin Android application for manga and anime enthusiasts, featuring AI-powered tools, local media management, and seamless online content discovery built with modern Android architecture.

### 🚀 Features

#### Core Functionality
- **The Vault**: Local media management with support for .cbz/.cbr manga and .mp4/.mkv/.avi anime files
- **AI Core**: OCR translation, art style matching, and AI-powered recommendations
- **The Browser**: Online content discovery with extensible source system
- **Clean Architecture**: MVVM pattern with Repository pattern, sealed Result classes
- **Modern UI**: Jetpack Compose with Material 3 design system

#### AI-Powered Features (In Development)
- **Scene Recommender**: AI-driven scene and chapter recommendations
- **AR Cosplay**: Augmented reality cosplay assistance and matching
- **AI Voice Reader**: Text-to-speech for manga with AI voice generation
- **Theme Matcher**: Intelligent theme and mood detection
- **Art Style Generator**: Generate artwork in similar styles
- **Waifu/Husbando Matcher**: Character personality and preference matching
- **Quote Bot**: Memorable quote extraction and sharing
- **Trivia System**: Interactive trivia based on consumed content
- **Relationship Maps**: Character relationship visualization
- **Episode Companion**: Context-aware episode information
- **Mood Tracker**: Reading/watching mood analysis and recommendations

### 🛠️ Technology Stack

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern Android UI toolkit
- **Material 3** - Latest Material Design system
- **MVVM + Clean Architecture** - Separation of concerns
- **Room Database** - Local data persistence
- **Hilt** - Dependency injection
- **Retrofit** - Network operations
- **Coil** - Image loading
- **StateFlow/SharedFlow** - Reactive data streams
- **Navigation Compose** - Type-safe navigation
- **Firebase** - Authentication, cloud sync, analytics
- **Coroutines** - Asynchronous programming

### 🏗️ Architecture

```
app/
├── data/                   # Data layer
│   ├── database/          # Room database
│   ├── repository/        # Repository implementations
│   └── network/          # API services
├── domain/               # Business logic layer
│   ├── entities/         # Core entities
│   ├── repository/       # Repository interfaces
│   ├── usecases/        # Business use cases
│   └── models/          # Domain models (Result, etc.)
├── ui/                  # Presentation layer
│   ├── screens/         # Compose screens
│   ├── components/      # Reusable UI components
│   ├── navigation/      # Navigation setup
│   ├── theme/          # Material 3 theming
│   └── viewmodel/      # MVVM ViewModels
├── di/                 # Dependency injection modules
└── utils/             # Utility functions
```

### 🚀 Getting Started

#### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 11 or higher
- Android SDK API 21-35
- Kotlin 1.9.22

#### Installation
```bash
# Clone the repository
git clone https://github.com/Heartless-Veteran/Project-Myriad.git
cd Project-Myriad

# Open in Android Studio and sync project
# Build and run on Android device/emulator
```

#### Building for Release
```bash
# Build release APK
cd android && ./gradlew assembleRelease

# The APK will be generated in:
# android/app/build/outputs/apk/release/app-release.apk
```

### 🧪 Testing
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedDebugAndroidTest
```

### 📦 Key Dependencies

#### Core Android
- `androidx.core:core-ktx` - Kotlin extensions
- `androidx.lifecycle:lifecycle-*` - Lifecycle components
- `androidx.activity:activity-compose` - Compose activity integration

#### UI & Design
- `androidx.compose:compose-bom` - Compose Bill of Materials
- `androidx.compose.material3:material3` - Material 3 components
- `androidx.navigation:navigation-compose` - Navigation component

#### Architecture
- `androidx.room:room-*` - Local database
- `com.google.dagger:hilt-android` - Dependency injection
- `com.squareup.retrofit2:retrofit` - Network client
- `io.coil-kt:coil-compose` - Image loading

#### Firebase & Cloud
- `com.google.firebase:firebase-bom` - Firebase services
- `com.google.firebase:firebase-auth-ktx` - Authentication
- `com.google.firebase:firebase-firestore-ktx` - Cloud database

#### AI & ML
- `com.google.mlkit:text-recognition` - OCR capabilities
- `com.google.mlkit:translate` - Translation services
- `io.github.sceneview:arsceneview` - AR functionality

### 🔧 Configuration

#### Firebase Setup
1. Create a Firebase project at https://console.firebase.google.com
2. Add Android app with package `com.projectmyriad`
3. Download `google-services.json` to `app/` directory
4. Enable Authentication, Firestore, and Storage

#### Local Development
- The app works offline-first with local Room database
- AI features gracefully degrade when services unavailable
- Firebase integration is optional for basic functionality

### 📋 Development Status

- [x] Core architecture implementation (Clean Architecture + MVVM)
- [x] Jetpack Compose UI with Material 3
- [x] Room database with sealed Result classes
- [x] Hilt dependency injection setup
- [x] Basic navigation and screens
- [ ] File import and management system
- [ ] AI feature implementations
- [ ] Firebase integration
- [ ] Online content sources
- [ ] Advanced UI features

### 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Follow Kotlin coding conventions and architecture patterns
4. Add tests for new functionality
5. Commit your changes (`git commit -m 'Add some amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### 📞 Support

For support and questions, please open an issue on GitHub.

---

**Project Myriad** - Bringing manga and anime content together with the power of AI and modern Kotlin Android development.