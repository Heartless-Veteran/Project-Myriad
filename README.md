# Project Myriad 🚀

## The Definitive Manga and Anime Platform

<div align="center">

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Android](https://img.shields.io/badge/platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/kotlin-3%20files-purple)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)
![Build Status](https://img.shields.io/badge/build-in%20progress-yellow.svg)

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

<table>
<tr>
<td valign="top" width="33%">

### **Core**
- **Kotlin 2.2.20** - Modern, expressive language
- **Android SDK 36** - Latest Android capabilities
- **Material 3** - Cutting-edge design system
- **Jetpack Compose** - Declarative UI toolkit

</td>
<td valign="top" width="33%">

### **Architecture**
- **Clean Architecture** - Separation of concerns
- **MVVM Pattern** - Reactive UI architecture  
- **Hilt** - Dependency injection
- **Room** - Local database persistence

</td>
<td valign="top" width="33%">

### **Networking**
- **Retrofit** - Type-safe HTTP client
- **OkHttp** - Efficient network operations
- **Kotlinx Serialization** - JSON parsing
- **Coroutines** - Asynchronous programming

</td>
</tr>
</table>

---

## 🔄 Development Workflow

**Automated Quality**: ktlint, Detekt, Android Lint, JaCoCo coverage with CI/CD on every push.

📖 **[Complete Workflow Documentation](docs/AUTOMATED_WORKFLOW.md)**

---

## 🏗️ Architecture

Project Myriad follows **Clean Architecture** principles with MVVM pattern, built using:

- **Presentation Layer**: Jetpack Compose + ViewModels + Type-safe Navigation
- **Domain Layer**: Use Cases + Repository Interfaces + Domain Models  
- **Data Layer**: Room Database + Retrofit API + File System

**Key Principles**: Single Source of Truth, Unidirectional Data Flow, Separation of Concerns

📖 **[Detailed Architecture Documentation](DEVELOPMENT.md#architecture-details)**

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

3. **Optional: Configure AI Features**
   ```bash
   cp local.properties.example local.properties
   echo "geminiApiKey=YOUR_API_KEY_HERE" >> local.properties
   ```

📖 **[Complete Development Setup Guide](DEVELOPMENT.md)**

---

## 📈 Roadmap

**Current Status**: Early development phase focusing on core architecture and foundation.

**Development Priorities**:
- 🏗️ **Foundation**: Core architecture and basic functionality *(In Progress)*
- 📚 **Core Features**: Media management and reader capabilities *(Planned)*
- 🤖 **AI Integration**: OCR translation and intelligent features *(Future)*
- ✨ **Enhanced UX**: Accessibility and user experience improvements *(Future)*

📋 **[Detailed Roadmap & Timeline](ROADMAP.md)**

---

## 🤖 AI-Powered Development

**Gemini Auto Enhanced**: AI-powered code fixing system with language-specific analysis and automated issue resolution.

📖 **[Complete Setup Guide & Documentation](docs/GEMINI_AUTO.md)**

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

**Current Test Coverage**: 15% (Expanding to 70%+ target)

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