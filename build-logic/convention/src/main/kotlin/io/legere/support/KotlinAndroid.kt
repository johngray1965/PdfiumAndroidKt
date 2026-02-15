package io.legere.support

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk = AppConfig.compileSdk

        defaultConfig.minSdk = AppConfig.minSdk

        compileOptions.sourceCompatibility = JavaVersion.VERSION_21
        compileOptions.targetCompatibility = JavaVersion.VERSION_21
        compileOptions.isCoreLibraryDesugaringEnabled = true

    }

    configureKotlin()

    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("desugar.jdk.libs").get())
    }
}

/**
 * Configure base Kotlin options for JVM (non-Android)
 */
internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        // Up to Java 11 APIs are available through desugaring
        // https://developer.android.com/studio/write/java11-minimal-support-table
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    configureKotlin()
}

/**
 * Configure base Kotlin options
 */
private fun Project.configureKotlin() {
    // Use withType to workaround https://youtrack.jetbrains.com/issue/KT-55947
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            // Set JVM target
            jvmTarget.set(JvmTarget.JVM_21) // Or your desired JVM target

            // Treat all Kotlin warnings as errors (disabled by default)
            // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
            // val warningsAsErrors: String? by project
            // allWarningsAsErrors.set(warningsAsErrors.toBoolean()) // You might want to enable this

            // Configure freeCompilerArgs
            // Start with a mutable list to easily add arguments
            val compilerArgs = mutableListOf<String>()

            // Add your standard opt-ins
            compilerArgs.addAll(
                listOf(
                    "-opt-in=kotlin.RequiresOptIn"
                    // Enable experimental coroutines APIs, including Flow
                )
            )

            // Add the new opt-in
            compilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")

            // Add any other specific compiler arguments you need globally
            // compilerArgs.add("-Xjvm-default=all") // Example

            freeCompilerArgs.addAll(compilerArgs) // Use addAll to append
        }
    }
}
