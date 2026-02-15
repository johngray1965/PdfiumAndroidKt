/*
 * Copyright (c) 2025.  Legere. All rights reserved.
 */

package io.legere.support

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra // For project.rootProject.extra

@Suppress("LongMethod")
internal fun Project.configureLint(commonExtension: CommonExtension) {
    // Retrieve the prefix from root project's extra properties
    // It's crucial that rootProject.extra is populated when this function is called.
    // If you still face issues with "extra property not found", wrap this access
    // and the conditional logic in project.afterEvaluate { ... }
    var isMediaModule = false // Default to false
    val mediaModulePrefixFromExtra =
        if (rootProject.extra.has("androidxMediaModulePrefix")) rootProject.extra["androidxMediaModulePrefix"] else null

    if (mediaModulePrefixFromExtra == null) {
//        logger.warn(
//            "LintConfig: 'androidxMediaModulePrefix' NOT FOUND on rootProject.extra for project $path. " +
//                "Lint will be configured with defaults for this project (not specifically disabled as media)."
//        )
    } else {
        val mediaModulePrefix = mediaModulePrefixFromExtra as? String
        if (mediaModulePrefix != null && name.startsWith(mediaModulePrefix)) {
            isMediaModule = true
        }
    }
    commonExtension.apply {
        if (isMediaModule) {
            logger.lifecycle("Configuring Lint to be minimal/disabled for media module: $name")
            lint.apply {
                // Settings to make Lint effectively a no-op or very minimal for media modules
                ignoreTestSources = true
                abortOnError = false // CRITICAL: Do not fail build for lint issues in media code
                checkAllWarnings = false
                checkReleaseBuilds = false
                checkDependencies = false // CRITICAL: Do not lint dependencies of media code
                checkTestSources = false
                explainIssues = false
                warningsAsErrors = false // CRITICAL: Do not treat warnings as errors
                showAll = false
                enable.clear() // Attempt to disable all specific checks (may not disable everything)
                checkOnly.clear() // Attempt to only check an empty set (may not disable everything)

                // You can try to disable all known checks if the above isn't enough.
                // This would require fetching all known check IDs, which is complex.
                // Setting a baseline to an empty file can also suppress issues.
                // baseline = file("$rootDir/config/lint/empty-lint-baseline.xml") // Ensure this file exists and is empty

                // Explicitly clear any pre-configured check sets (if any were inherited)
                fatal.clear()
                error.clear()
                warning.clear()
                informational.clear()
//            ignore.clear() // Clear any explicit ignores too, as we want to effectively disable

                // Set severity for everything to IGNORE if possible (more robust)
                // This is a more direct way to suppress issues if enable.clear()/checkOnly.clear() aren't fully effective.
                // The exact API for this can vary slightly with AGP versions.
                // For modern AGP, you might iterate through known issues if you could get them,
                // but a simpler approach is to use a lint.xml that ignores everything.
                lintConfig = file("$rootDir/config/lint/lint-ignore-all.xml") // See content below

                // Reports - probably don't need them for media modules if lint is "disabled"
                htmlReport = false
                textReport = false
                xmlReport = false
            }
        } else {
//        logger.lifecycle("Configuring standard Lint for project: $name")
            lint.apply {
                ignoreTestSources = true
                abortOnError = true
                checkAllWarnings = false
                checkReleaseBuilds = false
                checkDependencies = false
                disable += "PrivateResource"

                lintConfig = file("$rootDir/config/lint/lint.xml")
                showAll = true
                warningsAsErrors = true
//        htmlOutput = file("${project.layout.buildDirectory.get()}/reports/lint/lint-report.html")
//        htmlReport = true
//        textOutput = file("stdout")
//        textReport = true
//        xmlOutput = file("${project.layout.buildDirectory.get()}/reports/lint/lint-report.xml")
//        xmlReport = true
            }
        }
    }
    // --- Task Disabling Section ---
    // This part MUST run after the project is evaluated to safely find and disable tasks.
    if (isMediaModule) {
        // Defer task manipulation until after project evaluation
        project.afterEvaluate {
            logger.info("Attempting to disable Lint tasks for media module $name (afterEvaluate)...")
            project.tasks.configureEach {
                // Be careful with broad name matching if other tasks might share the "lint" prefix
                // This is generally okay for Lint, but good to be aware.
                if (name.startsWith("lint", ignoreCase = true) ||
                    name.contains("Lint", ignoreCase = true) &&
                    !name.startsWith("ktlint") // Avoid disabling ktlint tasks if they share the prefix
                ) {
                    // You could add more checks here if needed, e.g., task type,
                    // but for Lint tasks, name-based is usually sufficient.
                    logger.lifecycle("Disabling task by name: $name for media module $name")
                    enabled = false
                }
            }
        }
    }
}
