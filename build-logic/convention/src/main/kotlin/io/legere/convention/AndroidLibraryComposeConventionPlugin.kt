package io.legere.convention

import com.android.build.api.dsl.LibraryExtension
import io.legere.support.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("io.legere.convention.library")
                apply("org.jetbrains.kotlin.plugin.compose") // Ensure project build.gradle declared this plugin
            }

            val extension = extensions.getByType<LibraryExtension>()
            extension.apply {
                defaultConfig {
                    manifestPlaceholders["crashlyticsEnabled"] = false
                }
            }
            configureAndroidCompose(extension)
        }
    }
}
