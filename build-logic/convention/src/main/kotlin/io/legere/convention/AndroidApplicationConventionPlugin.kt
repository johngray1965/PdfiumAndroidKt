package io.legere.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import io.legere.support.AppConfig
import io.legere.support.common
import io.legere.support.configureBuildTypes
import io.legere.support.configureKotlinAndroid
import io.legere.support.configureLint
import io.legere.support.configurePackaging
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

fun isHostArm(): Boolean {
    val osArch = System.getProperty("os.arch")
    return osArch.contains("aarch64") || osArch.contains("arm64")
}

@Suppress("UnstableApiUsage")
class AndroidApplicationConventionPlugin : Plugin<Project> {
    @Suppress("LongMethod", "MagicNumber")
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            val gitInfo = getGitCommitCount(project)
            val gitVersionCode = gitInfo.first

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                common()
                configureBuildTypes(this)
                configurePackaging(this)
                configureLint(this)
                defaultConfig {
                    targetSdk = AppConfig.targetSdk
                    versionCode = gitVersionCode

                    vectorDrawables.useSupportLibrary = true
                }

                buildFeatures {
                    viewBinding = true
                    aidl = true
                    compose = true
                    buildConfig = true
                }
                testOptions {
                    execution = "ANDROIDX_TEST_ORCHESTRATOR"
                    animationsDisabled = true
                    unitTests {
                        isIncludeAndroidResources = true
                        isReturnDefaultValues = true
                    }
                    testFixtures {
                        enable = true
                    }
                    dependenciesInfo {
                        includeInApk = false
                        includeInBundle = true
                    }

                    managedDevices {
                        allDevices.apply {
                            val testedAbiStr =
                                if (isHostArm()) {
                                    "arm64-v8a"
                                } else {
                                    "x86_64"
                                }
                            register("pixel2Aosp", ManagedVirtualDevice::class.java) {
                                device = "Pixel 2"
                                apiLevel = 34
                                systemImageSource = "aosp-atd"
                                testedAbi = testedAbiStr
                            }
                            register("pixel7GoogleApis", ManagedVirtualDevice::class.java) {
                                device = "Pixel 7"
                                apiLevel = 34
                                systemImageSource = "google_apis"
                                testedAbi = testedAbiStr
                            }
                            register("pixelPphone", ManagedVirtualDevice::class.java) {
                                device = "Pixel 7"
                                apiLevel = 34
                                systemImageSource = "google-atd"
                                testedAbi = testedAbiStr
                            }
                            // 7" Tablet
                            register("pixelTablet7", ManagedVirtualDevice::class.java) {
                                device = "Pixel Tablet" // Or a specific 7" tablet definition like "Nexus 7"
                                apiLevel = 34
                                systemImageSource = "google-atd"
                                testedAbi = testedAbiStr
                            }
                            // 10" Tablet
                            register("pixelTablet10", ManagedVirtualDevice::class.java) {
                                device = "Pixel C" // A good 10" tablet example
                                apiLevel = 34
                                systemImageSource = "google-atd"
                                testedAbi = testedAbiStr
                            }
                            // Chromebook
                            register("pixelbook", ManagedVirtualDevice::class.java) {
                                device = "Desktop" // A good 10" tablet example
                                apiLevel = 34
                                systemImageSource = "google-atd"
                                testedAbi = testedAbiStr
                            }
                        }
                        groups {
                            create("screenshotDevices") {
                                targetDevices.add(allDevices.getByName("pixelPphone"))
                                targetDevices.add(allDevices.getByName("pixelTablet7"))
                                targetDevices.add(allDevices.getByName("pixelTablet10"))
//                        targetDevices.add(allDevices.getByName("pixelbook"))
                            }
                        }
                    }
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }

    @Suppress("ReturnCount", "MagicNumber")
    fun getGitCommitCount(project: Project): Pair<Int, String> { // Pass Project to access providers and projectDir

        val resultProvider =
            project.providers.exec {
                // 'this' inside this lambda is the ExecSpec
                commandLine("git", "rev-list", "--count", "HEAD")
                workingDir(project.projectDir) // Explicitly set working directory
                isIgnoreExitValue = true // Allow us to inspect the result even on failure
            }

        val gitCommandResult = resultProvider.result.get() // .get() here will execute it

        // Determine output based on success or failure
        val output =
            if (gitCommandResult.exitValue == 0) {
                resultProvider.standardOutput.asText
                    .get()
                    .trim()
            } else {
                val errorText =
                    resultProvider.standardError.asText
                        .get()
                        .trim()
                errorText.ifEmpty {
                    resultProvider.standardOutput.asText
                        .get()
                        .trim() // Some tools output errors to stdout
                }
            }

        if (gitCommandResult.exitValue != 0) {
            println("WARNING: 'git rev-list --count HEAD' failed in project ${project.name} with exit code ${gitCommandResult.exitValue}.")
            println("Git command output/error: $output")
            println("Working directory was: ${project.projectDir}")
            println("Make sure you are in a git repository and git commands can be run by Gradle.")
            // Return a default/fallback version code and the error output or a default string
            return Pair(99999, "Git command failed: $output") // Or throw an exception if this should be fatal
        }

        val commitCount = output.filter { it.isDigit() }.toIntOrNull()
        if (commitCount == null) {
            println("WARNING: Could not parse git commit count from output: '$output'")
            return Pair(99998, "Could not parse output: $output") // Different fallback for parsing error
        }
        return Pair(commitCount, output)
    }
}
