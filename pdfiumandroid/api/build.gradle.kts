/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023-2026 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.kotlin.dsl.create

plugins {
    id("io.legere.convention.library")
    id("io.legere.convention.static.analysis")
    id("io.legere.convention.kover")
    id("io.legere.convention.publish")

//    id("org.jetbrains.dokka")
//    id("org.jetbrains.dokka-javadoc")
}

fun isHostArm(): Boolean {
    val osArch = System.getProperty("os.arch")
    return osArch.contains("aarch64") || osArch.contains("arm64")
}

configure<LibraryExtension> {
    namespace = "io.legere.pdfiumandroid.api"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["useTestStorageService"] = "true"
        consumerProguardFiles("consumer-rules.pro")

        testOptions {
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
            animationsDisabled = true
            unitTests {
                isIncludeAndroidResources = true
                isReturnDefaultValues = true
            }
            @Suppress("UnstableApiUsage")
            managedDevices {
                allDevices {
                    create("pixel2Aosp", ManagedVirtualDevice::class) {
                        device = "Pixel 2"
                        apiLevel = 34
//                        systemImageSource = "google-atd"
                        systemImageSource = "aosp-atd"
                    }
                    create("pixel7GoogleApis", ManagedVirtualDevice::class) {
                        device = "Pixel 7"
                        apiLevel = 32
                        systemImageSource = "google-atd"
//                        systemImageSource = "google-atd"
//                        systemImageSource = "aosp-atd"
                        testedAbi =
                            if (isHostArm()) {
                                // On ARM hosts, prefer arm64-v8a for this device if your app supports it
                                "arm64-v8a"
                            } else {
                                // On x86_64 hosts, use x86_64 to avoid translation warning
                                "x86_64"
                            }
                    }
                    create("pixelPhone", ManagedVirtualDevice::class) {
                        device = "Pixel 7"
                        apiLevel = 34
                        systemImageSource = "google-atd"
                        testedAbi =
                            if (isHostArm()) {
                                // On ARM hosts, prefer arm64-v8a for this device if your app supports it
                                "arm64-v8a"
                            } else {
                                // On x86_64 hosts, use x86_64 to avoid translation warning
                                "x86_64"
                            }
                    }
                    // 7" Tablet
                    create("pixelTablet7", ManagedVirtualDevice::class) {
                        device = "Pixel Tablet" // Or a specific 7" tablet definition like "Nexus 7"
                        apiLevel = 34
                        systemImageSource = "google-atd"
                    }
                    // 10" Tablet
                    create("pixelTablet10", ManagedVirtualDevice::class) {
                        device = "Pixel C" // A good 10" tablet example
                        apiLevel = 34
                        systemImageSource = "google-atd"
                    }
                    // Chromebook
//                    create("pixelbook", ManagedVirtualDevice::class) {
//                        device = "Desktop" // A good 10" tablet example
//                        apiLevel = 34
//                        systemImageSource = "google-atd"
//                    }
                }
                groups {
                    // Group all the devices needed for screenshots
                    create("screenshotDevices") {
                        targetDevices.add(allDevices.getByName("pixelPhone"))
                        targetDevices.add(allDevices.getByName("pixelTablet7"))
                        targetDevices.add(allDevices.getByName("pixelTablet10"))
//                        targetDevices.add(allDevices.getByName("pixelbook"))
                    }
                }
            }
        }
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
//    dokkaPlugin(libs.android.documentation.plugin)
    implementation(libs.guava)
    api(libs.geokt)
    testImplementation(libs.junit)
    testImplementation(libs.espresso.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.core.testing)
    testImplementation(libs.bundles.test)
    testImplementation(libs.ext.junit)
    testImplementation(libs.robolectric)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.vintage.engine)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.core.testing)
    androidTestImplementation(libs.bundles.instrumented.non.ui.test)
    androidTestImplementation(libs.ext.junit)
    androidTestUtil(libs.androidx.orchestrator)
    androidTestUtil(libs.androidx.test.services)
}

publishPlugin {
    artifactId.set("pdfiumandroid-api")
    name.set("pdfiumandroid.api")
    description.set("Common APIs for PdfiumAndroid")
}
