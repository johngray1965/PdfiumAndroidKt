// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://jitpack.io")
        maven(url = "https://repo.spring.io/libs-release/")
        flatDir {
            dirs("libs")
        }
    }
    dependencies {
//        classpath("com.android.tools:r8:8.2.47") //
        classpath(libs.google.services)
//        classpath(libs.firebase.perf.plugin)
        classpath(libs.firebase.crashlytics.gradle)
        classpath(libs.navigation.safe.args.gradle.plugin)
        classpath(libs.kotlin.gradlePlugin)
        classpath(libs.detekt.gradlePlugin)
        classpath(libs.hilt.android.gradle.plugin)
//        classpath(libs.gradle.android.command.plugin)
        classpath(libs.android.junit5)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
    // alias(libs.plugins.ktlint)
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.benchmark) apply false
//    id("org.jetbrains.dokka") version "2.2.0-Beta"
//    id("org.jetbrains.dokka-javadoc") version "2.2.0-Beta"
}

// Configure the root 'dokkaGenerateHtml' task for the entire project.
// This task will automatically depend on and aggregate all 'html' publications
// from subprojects that also have the Dokka plugin applied and configured with dokkaPublications.html.
// dokka {
//    dokkaPublications.html {
//        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
//    }
// }

gitSemVer {
    buildMetadataSeparator.set("-")
}
