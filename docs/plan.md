# Project Myriad Improvement Plan (Native Android)

## Introduction

This document outlines the strategic improvement plan for Project Myriad, following its transition to a native Android application built with Kotlin and Jetpack Compose. The plan is aligned with modern Android development practices and focuses on delivering a best-in-class user experience.

## 1. Foundational Architecture (Phase 1)

**Rationale:** A strong foundation is critical for long-term scalability and maintainability. This phase establishes the core architectural patterns.

**Proposed Changes:**
- **Clean Architecture Setup:** Implement the `data`, `domain`, and `ui` layers with strict separation of concerns.
- **Dependency Injection with Hilt:** Set up Hilt modules for providing dependencies across the application (e.g., repositories, use cases, database).
- **Core Data Models:** Define the primary entities in the `domain` layer (e.g., `Manga`, `Chapter`, `Anime`).
- **Room Database Implementation:** Create the Room database, DAOs, and entities for local persistence. DAOs will use Kotlin Flow for reactive data streams.
- **Basic UI Shell:** Implement the main navigation structure using Jetpack Navigation Compose with a bottom bar for Library, Browse, and Settings.

## 2. The Vault - Local Media Engine (Phase 2)

**Rationale:** The core offline functionality is a primary feature. This phase focuses on building a robust local library experience.

**Proposed Changes:**
- **File System Abstraction:** Create a service to handle file system operations for importing and managing manga/anime files.
- **Library Screen:** Develop the main library UI in Jetpack Compose, displaying manga and anime from the Room database in a grid.
- **Metadata Service:** Implement a `MetadataService` that can scrape details for local files and save them to the database.
- **Reader/Player UI:**
    - **Manga Reader:** Build a Compose-based reader with vertical scrolling, zoom gestures, and support for `.cbz`/`.cbr` files.
    - **Anime Player:** Integrate a media player (e.g., ExoPlayer) within a Compose UI for playing local video files.

## 3. The Browser - Online Discovery (Phase 3)

**Rationale:** Expanding beyond the local library is key to user growth and content discovery. This phase builds the online browsing engine.

**Proposed Changes:**
- **Source Extension Framework:**
    - Define a `Source` interface in the `domain` layer (e.g., `getLatestManga()`, `getMangaDetails()`).
    - Implement a `SourceManager` to load and manage different source implementations.
- **Retrofit & Networking Layer:** Set up Retrofit with Kotlinx Serialization for API communication.
- **Browse Screen:** Create a UI that allows users to browse content from enabled sources, with features like search and filtering.
- **AniList/MyAnimeList Integration:** Implement a service to sync reading/watching progress with external tracking sites.

## 4. AI Core ("Yume") & Smart Features (Phase 4)

**Rationale:** The AI core is the app's signature feature. This phase brings "Yume," the AI companion, to life with intelligent and unique capabilities.

**Proposed Changes:**
- **Recommendation Engine:**
    - Develop a `RecommendationUseCase` that analyzes the user's library and reading history.
    - Yume will provide personalized suggestions on the main screen.
- **On-Device OCR & Translation:**
    - Integrate a lightweight, on-device ML model (e.g., using TensorFlow Lite) for real-time text recognition in the manga reader.
    - Add a feature to overlay translations on manga panels.
- **Art Style Analysis:**
    - Implement a computer vision model that generates a feature vector from manga cover art.
    - Allow users to find other series with a similar "art style hash."
- **Sakuga Detection (for Anime):**
    - Brainstorm and prototype an algorithm to detect scenes with high animation fidelity (sakuga) by analyzing motion vectors and frame complexity.
    - Allow users to bookmark these moments.

## 5. User Experience & Polish (Phase 5)

**Rationale:** A polished and customizable UX is what separates a good app from a great one. This phase is dedicated to refinement.

**Proposed Changes:**
- **Advanced Reader/Player Settings:**
    - Reading direction (LTR, RTL, Vertical), background color customization.
    - Subtitle controls and audio track selection for the video player.
- **Design System & Theming:**
    - Solidify the Material 3 design system with custom colors and typography.
    - Implement dynamic theming (e.g., theme based on the current manga's cover art).
- **Accessibility:** Conduct an accessibility audit, ensuring support for screen readers and sufficient color contrast.
- **Animations & Transitions:** Implement fluid screen transitions and micro-interactions using Jetpack Compose's animation APIs.

## 6. Testing, Performance & CI/CD (Ongoing)

**Rationale:** Quality assurance and performance are not phases but continuous processes.

**Proposed Changes:**
- **Unit & Integration Testing:** Write unit tests for ViewModels, UseCases, and Repositories. Use Turbine for testing Flows.
- **Performance Monitoring:** Profile the app for performance bottlenecks, especially in list scrolling and image loading.
- **CI/CD Pipeline:** Set up a GitHub Actions workflow to build and test the app on every push/PR.

## Conclusion

This improvement plan provides a comprehensive roadmap for enhancing Project Myriad across all aspects of the application. By implementing these changes, we will create a robust, user-friendly platform that meets the needs of manga and anime enthusiasts while leveraging cutting-edge technology for an optimal experience.

The plan prioritizes improvements that will have the most significant impact on user experience and application stability, while also laying the groundwork for future enhancements. Regular evaluation of progress against this plan will ensure that development remains focused and effective.