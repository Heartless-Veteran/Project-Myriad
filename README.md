# Project Myriad
## The Definitive Manga and Anime Platform for Android

**Project Myriad** is a next-generation, open-source Android application for manga and anime enthusiasts. Built entirely with Kotlin and Jetpack Compose, it offers a clean, performant, and deeply engaging experience, featuring a powerful local media engine and a visionary AI core.

### 🚀 Features

#### 🏠 The Vault - Local Media Engine
- **Offline-First Library:** Manage and read your local manga (`.cbz`, `.cbr`) and watch anime (`.mp4`, `.mkv`).
- **Seamless Imports:** Easily import your existing collection.
- **Metadata Engine:** Automatically scrapes and organizes metadata for your library.
- **Smart Synchronization:** Keep your reading progress and library in sync (future goal).

#### 🧠 AI Core - "Yume" The AI Companion
- **AI-Powered Recommendations:** Yume learns your tastes to suggest new manga and anime you'll love.
- **Interactive Translation:** On-device OCR to translate manga text in real-time.
- **Art Style Analysis:** Discover new series with similar art styles.
- **Natural Language Search:** "Show me a fantasy manga with a strong female lead" just works.
- **Sakuga Detection:** Yume can identify and bookmark sequences of high-quality animation in anime.

#### 🌐 The Browser - Online Discovery Engine
- **Extensible Source System:** A plugin-style architecture to browse and integrate with various online manga and anime sources.
- **Unified Search:** Search across all your enabled sources at once.
- **Track & Sync:** Integrate with services like AniList and MyAnimeList to track your progress automatically.

### 📱 Platform & Technology
- **100% Kotlin:** Leveraging the full power of the Kotlin language.
- **Jetpack Compose:** Modern, declarative UI for a beautiful and responsive interface.
- **Clean Architecture & MVVM:** A robust and scalable architecture for maintainability.
- **Coroutines & Flow:** For efficient, structured concurrency.
- **Hilt:** For robust dependency injection.
- **Room:** For persistent, local database storage.
- **Retrofit & Kotlinx Serialization:** For type-safe networking.
- **Coil:** For fast and efficient image loading.
- **Jetpack Navigation:** For navigating between screens.

### 🏗️ Project Structure
```
src/
├── data/
│   ├── local/          # Room DB, DAOs, local data sources
│   ├── remote/         # Retrofit services, remote data sources
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Core data models (Manga, Chapter, etc.)
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic, use cases
├── ui/
│   ├── components/     # Reusable Compose components
│   ├── navigation/     # Navigation graph and routes
│   ├── screens/        # Feature screens (library, reader, settings)
│   └── theme/          # M3 Theme, colors, typography
└──di/                 # Hilt dependency injection modules
```

### 🚀 Getting Started

#### Prerequisites
- Android Studio (latest stable version)
- JDK 17 or higher

#### Installation & Running
```bash
# Clone the repository
git clone https://github.com/Heartless-Veteran/Project-Myriad.git
cd Project-Myriad

# Open the project in Android Studio
# Let Gradle sync and download dependencies

# Run on an Android device/emulator
# Select the 'app' run configuration and click 'Run'
```

### 🧪 Testing
The project uses JUnit and Turbine for testing.
```bash
# Run unit tests from Android Studio or via Gradle
./gradlew test
```

### 📋 Documentation
- [Development Guide](docs/DEVELOPMENT.md)
- [Improvement Plan](docs/plan.md)
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

**Project Myriad** - Bringing manga and anime content together with the power of AI and modern mobile technology.
