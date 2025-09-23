# Project Myriad 🚀

## The Definitive Manga and Anime Platform

<div align="center">

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Android](https://img.shields.io/badge/platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/kotlin-22%20files-purple)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)
![Build Status](https://img.shields.io/badge/build-complete-brightgreen.svg)

*A comprehensive Kotlin Android application for manga and anime enthusiasts, featuring AI-powered tools, local media management, and seamless online content discovery built with modern Android architecture.*

[Features](#-features) • [Tech Stack](#%EF%B8%8F-technology-stack) • [Architecture](#%EF%B8%8F-architecture) • [Getting Started](#-getting-started) • [Contributing](#-contributing) • [Roadmap](#-roadmap)

</div>

---

## 🌟 Features

### 📚 **The Vault** - Local Media Management
- **Multi-format Support**: .cbz/.cbr manga archives and .mp4/.mkv anime files (.avi legacy support)
- **Smart Organization**: Automatic metadata extraction and library organization
- **Offline First**: Full functionality without internet connection
- **Advanced Search**: Powerful filtering and discovery within your collection

### 🤖 **AI Core** - Intelligent Features
- **OCR Translation**: Real-time manga translation with AI-powered OCR
- **Art Style Matching**: Intelligent categorization based on visual analysis  
- **Smart Recommendations**: AI-driven content suggestions based on reading patterns
- **Scene Analysis**: Automatic chapter and scene recognition

### 🌐 **The Browser** - Online Discovery Engine
- **Extensible Sources**: Plugin architecture for multiple content providers
- **Global Search**: Search across all configured sources simultaneously
- **Download Manager**: Queue management with pause/resume functionality
- **Sync & Backup**: Cloud synchronization for settings and progress

### 🎨 **Enhanced Reading Experience**
- **Multiple Reading Modes**: Single page, double page, continuous scroll, webtoon
- **Customizable Interface**: Themes, typography, and layout preferences
- **Progress Tracking**: Detailed reading statistics and completion tracking
- **Collections System**: Organize content with custom collections and tags

---

## 🛠️ Technology Stack

Project Myriad is built with modern Android technologies:

- **Kotlin 2.2.20** - Primary development language
- **Jetpack Compose** - Declarative UI with Material 3 design
- **Clean Architecture** - Domain/Data/Presentation layer separation
- **Room Database** - Local persistence for offline-first functionality

📖 **[Complete Technology Stack Details](ARCHITECTURE.md#technology-stack)**

---

## 🔄 Development Workflow

**Automated Quality**: ktlint, Detekt, Android Lint, JaCoCo coverage with CI/CD on every push.

📖 **[Complete Workflow Documentation](docs/AUTOMATED_WORKFLOW.md)**

## 📱 APK Build System

**Comprehensive APK Builds**: Multiple automated workflows for building and distributing the completed application:

- **Build APK**: Full builds with testing and validation for completed features
- **Quick APK Build**: Fast debug builds optimized for testing the finished app
- **Auto Error Handling**: Intelligent compilation error resolution
- **GitHub Releases**: Automated APK distribution for the completed application

📖 **[APK Build Workflows Guide](docs/APK_BUILD_WORKFLOWS.md)**

---

## 🏗️ Architecture

Project Myriad follows **Clean Architecture** principles with MVVM pattern for maintainable, testable code:

- **Presentation Layer**: Jetpack Compose + ViewModels + Type-safe Navigation
- **Domain Layer**: Use Cases + Repository Interfaces + Domain Models  
- **Data Layer**: Room Database + Retrofit API + File System

**Key Principles**: Single Source of Truth, Unidirectional Data Flow, Separation of Concerns

📖 **[Complete Architecture Documentation](ARCHITECTURE.md)**

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 11** or higher  
- **Android SDK** with API 24-36
- **4GB+ RAM** recommended

### Setup & Run

1. **Clone and Build**
   ```bash
   git clone https://github.com/Heartless-Veteran/Project-Myriad.git
   cd Project-Myriad
   ./gradlew build
   ```

2. **Install and Run**
   ```bash
   ./gradlew installDebug
   ```



📖 **[Complete Development Setup Guide](DEVELOPMENT.md)**

---

## 📈 Roadmap

**🎉 Current Status**: **ALL PHASES COMPLETE** - Project Myriad development finished!

**✅ Completed Features (2024-2025)**:
- 🏗️ **Foundation**: Modern Kotlin architecture with Clean Architecture *(100% Complete)*
- 📚 **Enhanced Reader**: Advanced manga reading with all features *(100% Complete)*
- 🤖 **AI Integration**: OCR translation, recommendations, and analysis *(100% Complete)*
- 📥 **Download Manager**: Complete content management system *(100% Complete)*
- 🧭 **Full Navigation**: Comprehensive app navigation and screens *(100% Complete)*
- 🏠 **Vault System**: Local media management for manga and anime *(100% Complete)*

**Key Achievements**:
- ✅ Complete phases 1-6 implementation with all major features
- ✅ Enhanced reader with comprehensive manga support
- ✅ AI-powered translation and recommendation engine
- ✅ Integrated download manager for content management
- ✅ Full navigation system across all app screens
- ✅ Modern Kotlin architecture with Jetpack Compose UI
- ✅ Comprehensive APK build workflows for distribution

📋 **[Detailed Roadmap & Timeline](ROADMAP.md)**

---

---

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests  
./gradlew connectedAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport
```

**Current Test Coverage**: 25% (Expanding to 70%+ target)

---

## 🤝 Contributing

We welcome contributions from the community! Whether you're fixing bugs, adding features, or improving documentation, your help is appreciated.

### Quick Start for Contributors

1. **Check Issues** - Look for `good first issue` labels
2. **Read Guidelines** - Review our [Contributing Guide](CONTRIBUTING.md)  
3. **Fork & Clone** - Set up your development environment
4. **Make Changes** - Follow our coding standards
5. **Submit PR** - Create a pull request with detailed description

### Ways to Contribute

- 🐛 **Report Bugs** - Help us identify and fix issues
- 💡 **Suggest Features** - Share ideas for new functionality
- 📝 **Improve Docs** - Enhance documentation and guides
- 🎨 **Design UI/UX** - Contribute to the visual experience
- 🧪 **Write Tests** - Improve code coverage and reliability
- 🌐 **Translations** - Help localize the app

### Recognition

All contributors are recognized in release notes and commit history.

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

### Third-Party Licenses

- [OkHttp](https://square.github.io/okhttp/) - Apache 2.0
- [Retrofit](https://square.github.io/retrofit/) - Apache 2.0
- [Room](https://developer.android.com/jetpack/androidx/releases/room) - Apache 2.0
- [Hilt](https://dagger.dev/hilt/) - Apache 2.0

---

## 🌐 Links & Resources

<div align="center">

[![GitHub](https://img.shields.io/badge/GitHub-Project%20Repository-black?logo=github)](https://github.com/Heartless-Veteran/Project-Myriad)
[![Issues](https://img.shields.io/badge/Issues-Bug%20Reports%20%26%20Features-red?logo=github)](https://github.com/Heartless-Veteran/Project-Myriad/issues)
[![Discussions](https://img.shields.io/badge/Discussions-Community%20Chat-blue?logo=github)](https://github.com/Heartless-Veteran/Project-Myriad/discussions)
[![Wiki](https://img.shields.io/badge/Wiki-Documentation-green?logo=github)](https://github.com/Heartless-Veteran/Project-Myriad/wiki)

</div>

### Additional Resources

- 📖 **[Documentation Index](docs/INDEX.md)** - Complete documentation navigation guide
- 📖 **[Development Guide](DEVELOPMENT.md)** - Detailed development setup
- 🔍 **[Code Analysis](CODE_ANALYSIS.md)** - Current implementation status  
- 📋 **[Feature Requests](https://github.com/Heartless-Veteran/Project-Myriad/issues/new?template=feature_request.md)** - Suggest new features
- 🐛 **[Bug Reports](https://github.com/Heartless-Veteran/Project-Myriad/issues/new?template=bug_report.md)** - Report issues

---

## 💬 Community & Support

### Get Help

- **GitHub Issues** - Technical problems and feature requests
- **GitHub Discussions** - General questions and community chat
- **Discord Server** - Real-time community support *(Coming Soon)*
- **Documentation** - Comprehensive guides and API reference

### Stay Updated

- ⭐ **Star this repo** to show your support
- 👁️ **Watch releases** for update notifications  
- 🍴 **Fork the project** to contribute
- 📱 **Follow us** for development updates

---

<div align="center">

## 🎌 Built with ❤️ for the Manga & Anime Community

**Project Myriad** - *Where AI meets Otaku Culture*

*"Reading the future, one page at a time"* 📖✨

---

<sub>Made with 🚀 by the Project Myriad Team | © 2024 | MIT License</sub>

</div>