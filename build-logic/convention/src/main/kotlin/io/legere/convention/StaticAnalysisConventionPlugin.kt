/*
 * Copyright (c) 2025.  Legere. All rights reserved.
 */

package io.legere.convention

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.legere.support.configureDetekt // Assuming configureDetekt is in this package or imported
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra // For project.rootProject.extra

class StaticAnalysisConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Retrieve the prefix from root project's extra properties
        // This access happens when the plugin is applied to 'target' project.
        // It's crucial that rootProject.extra is populated when this plugin's apply() is called.
        val mediaModulePrefix =
            if (target.rootProject.extra.has(
                    "androidxMediaModulePrefix",
                )
            ) {
                target.rootProject.extra["androidxMediaModulePrefix"] as? String
            } else {
                null
            }

        // Check if the current 'target' project is one to be skipped
        if (mediaModulePrefix != null && target.name.startsWith(mediaModulePrefix)) {
//            target.logger.lifecycle(
//                "StaticAnalysisConventionPlugin is SKIPPING application to project: ${target.name} " +
//                    "(matches prefix '$mediaModulePrefix')"
//            )
            return // Do not apply any static analysis to this project
        }

//        target.logger.lifecycle("StaticAnalysisConventionPlugin is APPLYING to project: ${target.name}")

        // Proceed with applying plugins and configurations for projects that are NOT skipped
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")
//            pluginManager.apply("org.jlleitschuh.gradle.ktlint")
            // Add Ktlint configuration if you have a similar configureKtlint(target) function
            // e.g., extensions.configure<org.jlleitschuh.gradle.ktlint.reporter.ReporterExtension> { ... }

            // Configure Detekt (configureDetekt should NOT contain the exclusion logic anymore
            // if this plugin handles the exclusion at the top level)
            extensions.configure<DetektExtension> {
                // Call your existing configureDetekt, but ensure it no longer tries to
                // exclude based on the prefix, as this plugin now handles that.
                // Pass 'this' which is the DetektExtension, or 'target' if configureDetekt expects Project
                configureDetekt(target) // Assuming configureDetekt expects Project
            }
        }
    }
}
