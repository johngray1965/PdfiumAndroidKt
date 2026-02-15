import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.kotlin.dsl.create

plugins {
    id("io.legere.convention.library")
    id("io.legere.convention.static.analysis")
    id("io.legere.convention.kover")
    alias(libs.plugins.gradle.publish)
    `maven-publish`
    signing
}

val numShards: String = System.getenv("CIRCLE_NODE_TOTAL") ?: "0"
val shardIndex: String = System.getenv("CIRCLE_NODE_INDEX") ?: "0"

fun isHostArm(): Boolean {
    val osArch = System.getProperty("os.arch")
    return osArch.contains("aarch64") || osArch.contains("arm64")
}

configure<LibraryExtension> {
    namespace = "io.legere.pdfiumandroid.core"
    ndkVersion = "29.0.13846066"

    defaultConfig {

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

    publishing {
        singleVariant("release") {
            // if you don't want sources/javadoc, remove these lines
            withSourcesJar()
            withJavadocJar()
        }
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
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.legere"
            artifactId = "pdfiumandroid-api"
            version = project.property("VERSION_NAME") as String

            pom {
                name.set("pdfiumandroid.api")
                description.set("Common APIs for PdfiumAndroid")
                url.set(rootProject.properties["POM_URL"] as String)
                licenses {
                    license {
                        name.set(rootProject.properties["POM_LICENCE_NAME"] as String)
                        url.set(rootProject.properties["POM_LICENCE_URL"] as String)
                        distribution.set(rootProject.properties["POM_LICENCE_DIST"] as String)
                    }
                }
                developers {
                    developer {
                        id.set(rootProject.properties["POM_DEVELOPER_ID"] as String)
                        name.set(rootProject.properties["POM_DEVELOPER_NAME"] as String)
                    }
                }
                scm {
                    connection.set(rootProject.properties["POM_SCM_CONNECTION"] as String)
                    developerConnection.set(rootProject.properties["POM_SCM_DEV_CONNECTION"] as String)
                    url.set(rootProject.properties["POM_SCM_URL"] as String)
                }
            }
            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            url =
                uri(layout.buildDirectory.dir("target/staging-deploy"))
        }
    }
}
signing {
    val keyId = project.properties["signing.keyId"]?.toString() ?: ""
    val password = project.properties["signing.password"]?.toString() ?: ""

    // This attempts to read the keyring file specified in gradle.properties
    // which contains the full PGP key material.
    if (keyId.isNotBlank() && password.isNotBlank()) {
        sign(publishing.publications)
    }
}
group = rootProject.properties["GROUP"] as String

publishOnCentral {
    licenseName.set("The Apache License, Version 2.0")
    repoOwner.set("Your-GitHub-username") // Used to populate the default value for projectUrl and scmConnection
    projectDescription.set("Common APIs for PdfiumAndroid")
    // The following values are the default, if they are ok with you, just omit them
    projectLongName.set(project.name)
    licenseName.set("Apache License, Version 2.0")
    licenseUrl.set("http://www.apache.org/licenses/LICENSE-2.0")
    projectUrl.set("https://github.com/${repoOwner.get()}/${project.name}")
    scmConnection.set("scm:git:https://github.com/${repoOwner.get()}/${project.name}")

    /*
     * The publications can be sent to other destinations, e.g. GitHub
     * The task name would be 'publishAllPublicationsToGitHubRepository'
     */
//    val githubUsername = project.properties["GITHUB_USERNAME"]?.toString() ?: ""
//    val githubToken = project.properties["GITHUB_TOKEN"]?.toString() ?: ""
//    repository("https://maven.pkg.github.com/johngray1965/PdfiumAndroidKt", "GitHub") {
//        user.set(githubUsername)
//        password.set(githubToken)
//    }
}
