# Project Myriad 🚀

<!-- PROJECT_STATS -->
## 📊 Project Statistics

| Metric | Value |
|--------|-------|
| **Kotlin Files** | 62 |
| **Test Files** | 11 |
| **Lines of Code** | 8,474 |
| **Dependencies** | 52 |
| **Target SDK** | 35 |
| **Min SDK** | 24 |
| **Version** | 1.0.0 |
| **Commits** | 823 |
| **Contributors** | 0 |

*Last updated: 2025-08-31*

### Dependency Breakdown
- **Implementation**: 41 dependencies
- **Test**: 7 dependencies  
- **Android Test**: 4 dependencies

### Recent Activity
- Latest commit: 4fdbc7d Merge pull request #225 from Heartless-Veteran/copilot/fix-224

<!-- /PROJECT_STATS -->
## The Definitive Manga and Anime Platform

<div align="center">

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Android](https://img.shields.io/badge/platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/kotlin-62%20files-purple)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)
![Build Status](https://img.shields.io/badge/build-in%20progress-yellow.svg)

*A comprehensive Kotlin Android application for manga and anime enthusiasts, featuring AI-powered tools, local media management, and seamless online content discovery built with modern Android architecture.*

[Features](#-features) • [Tech Stack](#%EF%B8%8F-technology-stack) • [Architecture](#%EF%B8%8F-architecture) • [Getting Started](#-getting-started) • [Contributing](#-contributing) • [Roadmap](#-roadmap)

</div>

---

## 🌟 Features

### 📚 **The Vault** - Local Media Management
- **Multi-format Support**: .cbz/.cbr manga archives and .mp4/.mkv/.avi anime files
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
- **Kotlin** - Modern, expressive language
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

## 🔄 Automated Quality & Documentation

Project Myriad features a comprehensive **Automated Documentation & Code Quality Workflow** that ensures consistent code standards and up-to-date documentation:

### 🔍 **Quality Assurance**
- **ktlint**: Kotlin code style checking and auto-formatting
- **Detekt**: Static code analysis with auto-corrections
- **Android Lint**: Platform-specific issue detection
- **JaCoCo**: Test coverage reporting and tracking

### 📚 **Documentation Automation**
- **Dokka**: API documentation generation
- **README Statistics**: Automated project metrics updates
- **Architecture Validation**: Clean Architecture compliance checking
- **Dependency Tracking**: Current dependency documentation

### 🚀 **Automated Improvements**
- **Weekly Quality PRs**: Automated pull requests with improvements
- **Code Formatting**: Automatic style fixes
- **Documentation Updates**: Current project statistics and metrics
- **Architecture Compliance**: Validates layer separation and naming conventions

> 📋 **Workflow Status**: Runs on every push, PR, and weekly schedule. See [docs/AUTOMATED_WORKFLOW.md](docs/AUTOMATED_WORKFLOW.md) for details.

---

## 🏗️ Architecture

Project Myriad follows **Clean Architecture** principles with clear separation between layers:

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

### Key Architectural Principles

- **Single Source of Truth** - Room database as the authoritative data source
- **Unidirectional Data Flow** - Clear data flow from UI to data layer
- **Separation of Concerns** - Each layer has distinct responsibilities
- **Dependency Inversion** - Abstractions don't depend on concretions

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 11** or higher  
- **Android SDK** with API 36+
- **4GB+ RAM** recommended for smooth development

### Quick Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Heartless-Veteran/Project-Myriad.git
   cd Project-Myriad
   ```

2. **Configure API Keys** (Optional - for AI features)
   ```bash
   # Copy and edit local properties
   cp local.properties.example local.properties
   # Add your Gemini API key
   echo "geminiApiKey=YOUR_API_KEY_HERE" >> local.properties
   ```

3. **Build & Run**
   ```bash
   # Build the project
   ./gradlew build
   
   # Run on device/emulator
   ./gradlew installDebug
   ```

### Development Setup

For detailed development setup instructions, see our [Contributing Guide](CONTRIBUTING.md).

---

## 📱 Screenshots

<div align="center">

| Home Screen | Library View | Reading Interface |
|-------------|--------------|-------------------|
| ![Home](docs/images/home-screen.png) | ![Library](docs/images/library-view.png) | ![Reader](docs/images/reader-interface.png) |

| AI Features | Settings | Download Queue |
|-------------|----------|----------------|
| ![AI](docs/images/ai-features.png) | ![Settings](docs/images/settings-screen.png) | ![Downloads](docs/images/download-queue.png) |

*Screenshots will be updated as features are implemented*

</div>

---

## 📈 Roadmap

For detailed development plans, timelines, and implementation roadmap, see our comprehensive **[🗺️ ROADMAP.md](ROADMAP.md)**.

**Quick Overview:**
- **Phase 1 (Q1 2024)**: 🏗️ Foundation - Core architecture and basic functionality *(In Progress)*
- **Phase 2 (Q2 2024)**: 🚀 Core Features - Media management and reader capabilities *(Planned)*
- **Phase 3 (Q3 2024)**: 🤖 AI Integration - OCR translation and intelligent features *(Planned)*
- **Phase 4 (Q4 2024)**: ✨ Polish & UX - Accessibility and user experience *(Planned)*
- **Phase 5 (Q1 2025)**: 🔒 Security & Stability - Production readiness *(Planned)*

---

## 🤖 Gemini Auto Enhanced - AI Code Fixer

**Project Myriad** includes an advanced AI-powered code fixing system with **language-specific intelligence** that automatically identifies and resolves code issues in pull requests.

### ✨ Enhanced Features
- 🔧 **Language-Specific Analysis**: Specialized rules for Kotlin, JavaScript, TypeScript, and Shell
- 🧠 **Smart AI Selection**: Uses Gemini 1.5 Pro for complex tasks, Gemini 1.5 Flash for simple ones
- ⚙️ **Configurable Rules**: Custom `.gemini-rules.json` for project-specific preferences
- 📊 **Rich Reporting**: Categorized fixes with detailed statistics and educational explanations
- 🛡️ **Enhanced Security**: Advanced validation, rate limiting, and secure processing
- 🚀 **Direct Commits**: Applies fixes directly to your PR with detailed commit messages

### 🔍 Language-Specific Optimizations
- **Kotlin**: Null-safety, coroutine contexts, data classes, Compose performance
- **JavaScript/TypeScript**: Modern syntax, unused imports, async/await patterns
- **Shell Scripts**: Best practices, security patterns, shebang validation
- **Security Analysis**: Vulnerability detection and automatic fixes
- **Performance**: Language-specific optimization opportunities

### 🛠️ Setup Gemini Auto Enhanced
1. **Get API Key**: Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. **Add Secret**: Go to Settings > Secrets > `GEMINI_API_KEY`
3. **Custom Config**: Optional `.gemini-rules.json` for project-specific rules
4. **Run Locally**: Use `./gradlew geminiAutoFix` for local analysis
5. **Use**: Add `gemini-auto` label to any PR or let it run automatically

### 📈 Advanced Configuration
```json
{
  "kotlin": { "enabled": true, "rules": { "null-safety": true, "coroutine-context": true } },
  "fixCategories": { "security": true, "performance": true, "style": true },
  "modelSelection": { "defaultModel": "gemini-1.5-flash", "complexTaskModel": "gemini-1.5-pro" }
}
```

For complete setup and configuration options, see **[📖 Enhanced Gemini Auto Guide](docs/GEMINI_AUTO.md)**.

---

## 🎯 Key Features Comparison

| Feature | Project Myriad | Tachiyomi | Other Apps |
|---------|----------------|-----------|------------|
| **Modern UI (Material 3)** | ✅ | ❌ | ⚠️ |
| **AI-Powered Features** | ✅ | ❌ | ❌ |
| **Local File Support** | ✅ | ✅ | ⚠️ |
| **Online Sources** | 🚧 | ✅ | ✅ |
| **Download Manager** | 🚧 | ✅ | ✅ |
| **Clean Architecture** | ✅ | ❌ | ❌ |
| **Kotlin Compose** | ✅ | ❌ | ❌ |

*Legend: ✅ Implemented | 🚧 In Progress | ❌ Not Available | ⚠️ Limited*

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

## 📊 Project Statistics

<div align="center">

![GitHub code size](https://img.shields.io/github/languages/code-size/Heartless-Veteran/Project-Myriad)
![Lines of code](https://img.shields.io/tokei/lines/github/Heartless-Veteran/Project-Myriad)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/Heartless-Veteran/Project-Myriad)
![GitHub last commit](https://img.shields.io/github/last-commit/Heartless-Veteran/Project-Myriad)

</div>

- **Languages**: Kotlin (95%), XML (5%)
- **Architecture**: Clean Architecture + MVVM
- **Minimum API**: 24 (Android 7.0)
- **Target API**: 36 (Android 15+)
- **Test Coverage**: 15% (improving to 70%+)

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

All contributors are recognized in our [Hall of Fame](CONTRIBUTORS.md) and release notes.

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