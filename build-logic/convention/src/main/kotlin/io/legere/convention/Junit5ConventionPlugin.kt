package io.legere.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class Junit5ConventionPlugin : Plugin<Project> {
    private val androidJunit5PluginId = "de.mannodermaus.android-junit5"
//    private val applicationModuleName = "application"
//    private val excludedModules = listOf("moduleA") // Modules you want to exclude manually

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(androidJunit5PluginId)
            }
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                val bom = libs.findLibrary("junit.bom").get()
                "testImplementation"(platform(bom))

                "testImplementation"(libs.findLibrary("slf4j.api").get())
                "testImplementation"(libs.findLibrary("slf4j.android").get())

                "testImplementation"(libs.findLibrary("junit.jupiter").get())
                "testRuntimeOnly"(libs.findLibrary("junit.platform.launcher").get())
                "testRuntimeOnly"(libs.findLibrary("junit.vintage.engine").get())

                "testImplementation"(libs.findLibrary("junit.jupiter.api").get())
                "testRuntimeOnly"(libs.findLibrary("junit.jupiter.engine").get())

                "testImplementation"(libs.findLibrary("robolectric").get())

                "testImplementation"(libs.findLibrary("android.test.extensions").get())
            }
        }
    }
}
