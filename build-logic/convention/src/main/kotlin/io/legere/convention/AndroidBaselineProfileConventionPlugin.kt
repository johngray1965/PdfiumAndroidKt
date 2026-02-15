package io.legere.convention

import androidx.baselineprofile.gradle.producer.BaselineProfileProducerExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.api.dsl.TestExtension
import com.android.build.api.variant.TestAndroidComponentsExtension
import io.legere.support.AppConfig
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

@Suppress("UnstableApiUsage", "MagicNumber")
class AndroidBaselineProfileConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply("com.android.test")
                apply("androidx.baselineprofile")
            }

            extensions.configure<TestExtension> {
                namespace = "io.legere.baselineprofile"
                compileSdk = AppConfig.compileSdk
                defaultConfig.targetSdk = AppConfig.targetSdk
                defaultConfig.minSdk = 28
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
                testOptions {
                    managedDevices {
                        allDevices.register("pixel6Api34", ManagedVirtualDevice::class.java) {
                            device = "Pixel 6"
                            apiLevel = 34
                            systemImageSource = "google"
                        }
                    }
                }
            }

            // 2. NEW: Configure Baseline Profile
            extensions.configure<BaselineProfileProducerExtension> {
                // In Kotlin (.kt), we use .add() instead of +=
                managedDevices.add("pixel6Api34")
                useConnectedDevices = false
            }

            // 3. NEW: Configure Android Components (Target ID Wiring)
            // Note: Suppress UnstableApiUsage if needed via annotation on the class or function
            extensions.configure<TestAndroidComponentsExtension> {
                onVariants { variant ->
                    val artifactsLoader = variant.artifacts.getBuiltArtifactsLoader()

                    variant.instrumentationRunnerArguments.put(
                        "targetAppId",
                        variant.testedApks.map {
                            // Ensure we return a non-null String to satisfy the Provider contract
                            artifactsLoader.load(it)?.applicationId ?: ""
                        }
                    )
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", libs.findLibrary("ext.junit").get())
                add("implementation", libs.findLibrary("espresso.core").get())
                add("implementation", libs.findLibrary("androidx.uiautomator").get())
                add("implementation", libs.findLibrary("benchmark.junit").get())
            }
        }
    }
}
