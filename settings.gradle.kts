pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Project-Myriad"
include(":app")

// Core modules
include(":core:ui")
include(":core:domain")
include(":core:data")

// Feature modules
include(":feature:reader")
include(":feature:browser")
include(":feature:vault")
include(":feature:settings")
include(":feature:ai")

// Performance optimization module
include(":baselineprofile")