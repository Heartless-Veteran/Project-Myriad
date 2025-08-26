import java.io.ByteArrayOutputStream

/**
 * Gradle task to run Gemini Auto locally for code fixing
 */
tasks.register<Exec>("geminiAutoFix") {
    description = "Run Gemini Auto to fix code issues locally"
    group = "verification"
    
    // Set working directory
    workingDir = rootProject.projectDir
    
    // Base command
    commandLine("node", "./scripts/gemini-auto.js")
    
    doFirst {
        // Check if Node.js and npm are available
        try {
            exec {
                commandLine("node", "--version")
                standardOutput = ByteArrayOutputStream()
            }
        } catch (e: Exception) {
            throw GradleException("Node.js is required but not found. Please install Node.js.")
        }
        
        // Check if dependencies are installed
        if (!file("node_modules").exists()) {
            println("Installing npm dependencies...")
            exec {
                commandLine("npm", "install", "--legacy-peer-deps")
            }
        }
        
        // Only run on changed files when not on CI
        if (System.getenv("CI") != "true") {
            val changedFilesResult = ByteArrayOutputStream()
            try {
                exec {
                    commandLine("git", "diff", "--name-only", "HEAD")
                    standardOutput = changedFilesResult
                }
                val changedFiles = changedFilesResult.toString().trim()
                if (changedFiles.isNotEmpty()) {
                    println("Analyzing only changed files: $changedFiles")
                    // Note: This would require additional implementation in gemini-auto.js
                    // to support --files parameter
                }
            } catch (e: Exception) {
                println("Could not determine changed files, analyzing all files")
            }
        }
    }
    
    doLast {
        println("Gemini Auto fix task completed")
    }
}

/**
 * Task to validate Gemini Auto setup
 */
tasks.register<Exec>("geminiAutoCheck") {
    description = "Check if Gemini Auto is properly configured"
    group = "verification"
    
    workingDir = rootProject.projectDir
    commandLine("node", "./scripts/test-gemini-auto.js")
    
    doFirst {
        println("Checking Gemini Auto configuration...")
    }
}