# Project Myriad
## The Definitive Manga and Anime Platform

**Project Myriad** is a comprehensive Android application for manga and anime enthusiasts, featuring AI-powered tools, local media management, and seamless online content discovery.

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
- **React Native 0.80.2** with TypeScript
- **React Navigation** for seamless navigation
- **AsyncStorage** for local data persistence
- **SQLite** for structured data storage
- **Fast Image** for optimized image loading
- **WebView** for online content integration

### 🏗️ Project Structure
```
src/
├── components/          # Reusable UI components
├── screens/            # Application screens
├── navigation/         # Navigation configuration
├── services/           # Core business logic
│   ├── VaultService.ts    # Local media management
│   ├── AIService.ts       # AI-powered features
│   └── BrowserService.ts  # Online content discovery
├── types/              # TypeScript type definitions
├── utils/              # Utility functions
└── stores/             # State management
```

### 🚀 Getting Started

#### Prerequisites
- Node.js (>= 18.0.0)
- Android Studio with Android SDK
- React Native CLI
- Java Development Kit (JDK) 11 or higher

#### Installation
```bash
# Clone the repository
git clone https://github.com/your-username/Project-Myriad.git
cd Project-Myriad

# Install dependencies
npm install

# Note: The project uses .npmrc with legacy-peer-deps=true to handle React 19 compatibility and @react-native/babel-preset

# Start Metro bundler
npm start

# Run on Android device/emulator
npm run android
```

#### Building for Release
```bash
# Build release APK
npm run build:android

# The APK will be generated in:
# android/app/build/outputs/apk/release/app-release.apk
```

### 🧪 Testing
```bash
# Run tests
npm test

# Run linting
npm run lint
```

### 📦 Key Dependencies
- **@react-navigation/native**: Navigation framework
- **react-native-fs**: File system operations
- **react-native-sqlite-storage**: Local database
- **react-native-fast-image**: Optimized image loading
- **react-native-webview**: Web content integration
- **react-native-document-picker**: File picker functionality
- **react-native-zip-archive**: Archive handling

### 🔧 Configuration
The project uses:
- **TypeScript** for type safety
- **ESLint** for code quality
- **Prettier** for code formatting
- **Jest** for testing
- **Metro** for bundling
- **Hermes** for JavaScript engine

### 📋 Documentation
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

**Project Myriad** - Bringing manga and anime content together with the power of AI and modern mobile technology.
