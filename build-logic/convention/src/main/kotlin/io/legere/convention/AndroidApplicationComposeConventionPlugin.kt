@file:Suppress("unused")

package io.legere.convention

import com.android.build.api.dsl.ApplicationExtension
import io.legere.support.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("io.legere.convention.application")
                apply("org.jetbrains.kotlin.plugin.compose") // Ensure project build.gradle declared this plugin
            }

            val extension = extensions.getByType<ApplicationExtension>()
            configureAndroidCompose(extension)
        }
    }
}
