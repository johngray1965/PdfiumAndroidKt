package io.legere.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

abstract class JacocoConventionExtension {
    abstract val reportPackage: Property<String>
}

class JacocoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<JacocoConventionExtension>("jacocoConvention")

        with(project) {
            pluginManager.apply("jacoco")
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            val jacocoVersion = libs.findVersion("jacoco").get().toString()
            extensions.configure<JacocoPluginExtension> {
                toolVersion = jacocoVersion
            }

            plugins.withId("com.android.application") {
                extensions.configure<ApplicationExtension> {
                    buildTypes.getByName("debug").enableAndroidTestCoverage = true
                    testCoverage.jacocoVersion = jacocoVersion
                }
            }
            plugins.withId("com.android.library") {
                extensions.configure<LibraryExtension> {
                    buildTypes.getByName("debug").enableAndroidTestCoverage = true
                    testCoverage.jacocoVersion = jacocoVersion
                }
            }

            val classDirectoriesProvider =
                extension.reportPackage.map { reportPackage ->
                    val pkgPath = reportPackage.replace(".", "/")
                    listOf(
                        fileTree(layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")) {
                            include("**/$pkgPath/*.class")
                        },
                        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
                            exclude($$"**/*$DefaultImpls.class") // Exclude DefaultImpls
                            include("**/$pkgPath/*.class")
                        },
                        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                            // Legacy/Fallback
                            include("**/$pkgPath/*.class")
                        },
                        fileTree(layout.buildDirectory.dir("intermediates/classes/debug")) {
                            // Fallback for newer AGP if classes are merged here
                            include("**/$pkgPath/*.class")
                        },
                    )
                }

            val executionDataProvider =
                fileTree(
                    layout.buildDirectory.dir(
                        "intermediates/managed_device_code_coverage/debugAndroidTest",
                    ),
                ) {
                    include("**/*.ec")
                }


            val sourceDirectoriesProvider = files("$projectDir/src/main/java", "$projectDir/src/main/kotlin")

            project.tasks.register<JacocoReport>("jacocoAndroidTestReport") {
                group = "Reporting"
                description = "Generates Jacoco report for Android instrumentation tests."
                dependsOn("pixelPhoneDebugAndroidTest")

                onlyIf { extension.reportPackage.isPresent }

                classDirectories.setFrom(classDirectoriesProvider)
                executionData.setFrom(executionDataProvider)
                sourceDirectories.setFrom(sourceDirectoriesProvider)

                reports {
                    xml.required.set(true)
                    html.required.set(true)
                }

                doLast {
                    val reportPath =
                        reports.html.outputLocation
                            .get()
                            .asFile
                    val reportUrl = reportPath.resolve("index.html").toURI()
                    println("Jacoco report for '$name' generated at: $reportUrl")
                }
            }

            project.tasks.register<JacocoCoverageVerification>("jacocoAndroidTestCoverageVerification") {
                group = "Reporting"
                description = "Verifies JaCoCo coverage for Android instrumentation tests."
                dependsOn("pixelPhoneDebugAndroidTest")

                onlyIf { extension.reportPackage.isPresent }

                classDirectories.setFrom(classDirectoriesProvider)
                executionData.setFrom(executionDataProvider)
                sourceDirectories.setFrom(sourceDirectoriesProvider)
                violationRules {
                    rule {
                        limit {
                            minimum = 0.95.toBigDecimal()
                        }
                    }
                }
            }
        }
    }
}
