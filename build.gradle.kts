// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        // needed for jreleaser
        //noinspection UseTomlInstead
        classpath("jakarta.activation:jakarta.activation-api:2.1.4")
        //noinspection NewerVersionAvailable,UseTomlInstead
        classpath("org.glassfish.jaxb:jaxb-runtime:2.3.6")
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
    alias(libs.plugins.ktlint)
    alias(libs.plugins.jreleaser) apply false
}
