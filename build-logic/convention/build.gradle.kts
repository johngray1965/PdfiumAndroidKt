import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    `kotlin-dsl` // use gradlePlugin to register the plugin we created, which helps gradle to discover our plugins
    `java-gradle-plugin`
}

group = "io.legere.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    implementation(libs.com.google.devtools.ksp.gradle.plugin)
    implementation(libs.androidx.baselineprofile.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
}

gradlePlugin {
    plugins {
        create("androidApplicationCompose") {
            id = "io.legere.convention.application.compose"
            implementationClass = "io.legere.convention.AndroidApplicationComposeConventionPlugin"
        }

        create("androidApplication") {
            id = "io.legere.convention.application"
            implementationClass = "io.legere.convention.AndroidApplicationConventionPlugin"
        }

        create("androidLibrary") {
            id = "io.legere.convention.library"
            implementationClass = "io.legere.convention.AndroidLibraryConventionPlugin"
        }

        create("androidLibraryCompose") {
            id = "io.legere.convention.library.compose"
            implementationClass = "io.legere.convention.AndroidLibraryComposeConventionPlugin"
        }

        create("hilt") {
            id = "io.legere.convention.hilt"
            implementationClass = "io.legere.convention.AndroidHiltConventionPlugin"
        }

        create("baselineprofileplugin") {
            id = "io.legere.convention.baselineprofileplugin"
            implementationClass = "io.legere.convention.AndroidBaselineProfileConventionPlugin"
        }

        create("jvmLibrary") {
            id = "io.legere.convention.jvm.library"
            implementationClass = "io.legere.convention.JvmLibraryConventionPlugin"
        }

        create("staticAnalysis") {
            id = "io.legere.convention.static.analysis"
            implementationClass = "io.legere.convention.StaticAnalysisConventionPlugin"
        }

        create("kover") {
            id = "io.legere.convention.kover"
            implementationClass = "io.legere.convention.KoverPlugin"
        }

        create("jacoco") {
            id = "io.legere.convention.jacoco"
            implementationClass = "io.legere.convention.JacocoPlugin"
        }

        create("junit5") {
            id = "io.legere.convention.junit5"
            implementationClass = "io.legere.convention.Junit5ConventionPlugin"
        }
    }
}

tasks.named("clean") {
    actions.clear()

    doLast {
        val buildDir = layout.buildDirectory.get().asFile
        // deleteRecursively() is aggressive and returns false if it fails,
        // but won't throw the specific Gradle exception that halts your build.
        val success = buildDir.deleteRecursively()

        if (!success && buildDir.exists()) {
            println("Warning: Could not fully delete ${buildDir.path}. Android Studio may be holding a lock.")
        }
    }
}
