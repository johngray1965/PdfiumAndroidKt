package io.legere.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("dagger.hilt.android.plugin")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                "implementation"(libs.findLibrary("hilt.android").get())
                "ksp"(libs.findLibrary("hilt.compiler").get())
                "ksp"(libs.findLibrary("androidx.hilt.compiler").get())
                "ksp"(libs.findLibrary("kotlin.metadata.jvm").get())

                "testImplementation"(libs.findLibrary("hilt.android.testing").get())
                "kspTest"(libs.findLibrary("hilt.android.compiler").get())
                "kspTest"(libs.findLibrary("kotlin.metadata.jvm").get())

                "androidTestImplementation"(libs.findLibrary("hilt.android.testing").get())
                "kspAndroidTest"(libs.findLibrary("hilt.android.compiler").get())
                "kspAndroidTest"(libs.findLibrary("kotlin.metadata.jvm").get())

                "testFixturesImplementation"(libs.findLibrary("hilt.android.testing").get())
                "kspTestFixtures"(libs.findLibrary("hilt.android.compiler").get())
            }
        }
    }
}
