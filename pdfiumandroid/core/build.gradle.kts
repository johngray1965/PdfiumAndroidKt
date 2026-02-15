import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.kotlin.dsl.create

plugins {
    alias(libs.plugins.android.library)
}

val numShards: String = System.getenv("CIRCLE_NODE_TOTAL") ?: "0"
val shardIndex: String = System.getenv("CIRCLE_NODE_INDEX") ?: "0"

fun isHostArm(): Boolean {
    val osArch = System.getProperty("os.arch")
    return osArch.contains("aarch64") || osArch.contains("arm64")
}

android {
    namespace = "io.legere.pdfiumandroid.core"
    compileSdk {
        version = release(36)
    }
    ndkVersion = "29.0.13846066"

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
        testInstrumentationRunnerArguments.putAll(
            mapOf(
                "grant_permission_rule" to "true",
                "clearPackageData" to "true",
                "coverage" to "true",
                "disableAnalytics" to "true",
                "useTestStorageService" to "true",
                "numShards" to numShards,
                "shardIndex" to shardIndex,
            ),
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
//            version = "4.0.3"
        }
    }
}

dependencies {
    api(project(":pdfiumandroid:api"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

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
tasks.register<JacocoReport>("jacocoAndroidTestReport") {
    group = "Reporting"
    description = "Generates JaCoCo coverage report for Android instrumentation tests."

    // Set the execution data file from the Android instrumentation tests
    executionData.setFrom(
        fileTree(
            layout.buildDirectory.dir(
                "intermediates/managed_device_code_coverage/debugAndroidTest/pixelPhoneDebugAndroidTest",
            ),
        ) {
            include("**/*.ec") // Adjust based on your Android Gradle Plugin version/setup
        },
    )

    sourceDirectories.setFrom(files("$projectDir/src/main/java", "$projectDir/src/main/kotlin"))

    // Set the class directories to analyze
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")) {
            exclude("**/*")

            include("**/io/legere/pdfiumandroid/jni/*.class")
        },
        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
            exclude($$"**/*$DefaultImpls.class") // Exclude DefaultImpls
            include("**/io/legere/pdfiumandroid/jni/*.class")
        },
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            // Legacy/Fallback
            include("**/io/legere/pdfiumandroid/jni/*.class")
        },
        fileTree(layout.buildDirectory.dir("intermediates/classes/debug")) {
            // Fallback for newer AGP if classes are merged here
            exclude("**/*")
            include("**/io/legere/pdfiumandroid/jni/*.class")
        },
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    dependsOn("pixelPhoneDebugAndroidTest") // Ensure instrumentation tests run before report generation
}

tasks.register<JacocoCoverageVerification>("jacocoAndroidTestCoverageVerification") {
    group = "Reporting"
    description = "Verifies JaCoCo coverage for Android instrumentation tests."

    executionData.setFrom(
        fileTree(
            layout.buildDirectory.dir(
                "intermediates/managed_device_code_coverage/debugAndroidTest/pixelPhoneDebugAndroidTest",
            ),
        ) {
            include("**/*.ec")
        },
    )

    sourceDirectories.setFrom(files("$projectDir/src/main/java", "$projectDir/src/main/kotlin"))

    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
            exclude("**/*")
            include("**/io/legere/pdfiumandroid/jni/**/*.class")
        },
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            include("**/io/legere/pdfiumandroid/jni/**/*.class")
        },
    )

    violationRules {
        rule {
            limit {
                minimum = 0.95.toBigDecimal()
            }
        }
    }

    dependsOn("pixelPhoneDebugAndroidTest")
}
