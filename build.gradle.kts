// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.benchmark) apply false
    id("org.jetbrains.dokka") version "2.1.0"
    id("org.jetbrains.dokka-javadoc") version "2.1.0"
}

// Configure the root 'dokkaGenerateHtml' task for the entire project.
// This task will automatically depend on and aggregate all 'html' publications
// from subprojects that also have the Dokka plugin applied and configured with dokkaPublications.html.
dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
    }
}
