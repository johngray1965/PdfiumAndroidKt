pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenLocal()
        mavenCentral()
//        maven(url = "https://s01.oss.sonatype.org/content/groups/staging/")
    }
}

rootProject.name = "pdfiumandroid"
include(":app")
include(":pdfiumandroid")
include(":pdfiumandroid:arrow")
include(":pdfiumandroid:benchmark")
