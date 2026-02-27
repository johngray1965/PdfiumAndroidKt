/*
 * Copyright (c) 2025.  Legere. All rights reserved.
 */

package io.legere.support
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

internal fun configureDetekt(project: Project) {
    with(project) {
        extensions.getByType<DetektExtension>().apply {
            config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
        }
        tasks.withType<Detekt>().configureEach {
            reports {
                // observe findings in your browser with structure and code snippets
                html.required.set(true)
                // similar to the console output, contains issue signature to manually edit baseline files
                txt.required.set(true)
                // simple Markdown format
                md.required.set(true)
            }
        }
//        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        // TODO: Ensure these libraries are defined in libs.versions.toml before enabling them
        // dependencies.apply {
        //     // You can add more detektPlugins like shown below.
        //     add("detektPlugins", libs.findLibrary("arrow-detekt").get())
        //     add("detektPlugins", libs.findLibrary("compose-detekt").get())
        // }
    }
}
