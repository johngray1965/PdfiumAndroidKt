import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jreleaser.model.Active
import org.jreleaser.model.Signing


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.jreleaser)
    jacoco
    `maven-publish`
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xstring-concat=inline")
    }
}

detekt {
    config.setFrom(files("${rootProject.projectDir}/config/detekt.yml"))
}
val numShards: String = System.getenv("CIRCLE_NODE_TOTAL") ?: "0"
val shardIndex: String = System.getenv("CIRCLE_NODE_INDEX") ?: "0"

fun isHostArm(): Boolean {
    val osArch = System.getProperty("os.arch")
    return osArch.contains("aarch64") || osArch.contains("arm64")
}

android {
    namespace = "io.legere.pdfiumandroid"
    compileSdk = 36

    ndkVersion = "29.0.13846066"

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
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

        packaging {
            resources {
                excludes +=
                    listOf(
                        "META-INF/LICENSE.md",
                        "META-INF/LICENSE-notice.md",
                        "META-INF/NOTICE.md",
                        "META-INF/AL2.0",
                        "META-INF/LGPL2.1",
                    )
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
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            enableAndroidTestCoverage = true
        }
//        maybeCreate("qa")
//        getByName("qa") {
//            matchingFallbacks += listOf("release")
//            isMinifyEnabled = true
//            signingConfig = signingConfigs.getByName("debug")
//        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
//            version = "4.0.3"
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }
    testCoverage {
        jacocoVersion = "0.8.13"
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
    publishing {
        singleVariant("release") {
            // if you don't want sources/javadoc, remove these lines
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

    compileOnly(libs.kotlinx.coroutines.android)
    compileOnly(libs.androidx.annotation.jvm)
    compileOnly(libs.kotlin.stdlib)
    implementation(libs.guava)

    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.espresso.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.bundles.test)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.vintage.engine)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.bundles.instrumented.non.ui.test)
    androidTestUtil(libs.androidx.orchestrator)
    androidTestUtil(libs.androidx.test.services)

    kover(project(":pdfiumandroid:arrow"))
}
kover {
    reports {
        // filters for all report types of all build variants
        filters {
            excludes {
//                androidGeneratedClasses()
                packages(
                    "io.legere.pdfiumandroid.jni",
                )
//                annotatedBy(
                // compose preview
//                    "androidx.compose.ui.tooling.preview.Preview",
//                    // begin Hilt classes
//                    "javax.annotation.processing.Generated",
//                    "dagger.internal.DaggerGenerated",
//                    "dagger.hilt.android.internal.lifecycle.HiltViewModelMap\$KeySet",
//                    // end Hilt classes
//                    "kotlinx.serialization.SerialName",
//                )
                classes(
                    // begin excludes generated classes
                    "*.R",
                    "*.R$*",
                    "*.BuildConfig",
                    "*.Manifest",
                    "*.Manifest$*",
                    "io.legere.pdfiumandroid.unlocked.SystemLibraryLoader",
                )
            }
        }
        variant("debug") {
            xml {
                onCheck = true
            }
            html {
                onCheck = true
            }
            verify {
                rule {
                    minBound(95)
                }
            }
        }
    }
}

fun getRepositoryUsername(): String =
    if (rootProject.hasProperty("JRELEASER_MAVENCENTRAL_USERNAME")) {
        rootProject.properties["JRELEASER_MAVENCENTRAL_USERNAME"] as String
    } else {
        ""
    }

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.legere"
            artifactId = "pdfiumandroid"
            version = project.property("VERSION_NAME") as String

            pom {
                name.set("pdfiumandroid")
//                packaging = rootProject.properties["POM_PACKAGING"] as String
                description = rootProject.properties["POM_DESCRIPTION"] as String
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

jreleaser {
    project {
        inceptionYear = "2023"
        author("@johngray1965")
        description = rootProject.properties["POM_DESCRIPTION"] as String
        version = rootProject.properties["VERSION_NAME"] as String
    }
    gitRootSearch = true
    signing {
        active = Active.ALWAYS
        mode = Signing.Mode.MEMORY
        armored = true
        verify = true
    }
    release {
        github {
            repoOwner = "johngray1965"
            overwrite = true
        }
    }
//    distributions {
//        create("zip") {
//            artifacts {
//                add(
//                    layout.buildDirectory
//                        .dir("libs")
//                        .map {
//                            it.file("pdfiumandroid.zip")
//                        }
//                )
//            }
//        }
//    }
    deploy {
        maven {
            mavenCentral.create("sonatype") {
                active = Active.ALWAYS
                verifyPom = false
                url = "https://central.sonatype.com/api/v1/publisher"
                stagingRepository(
                    layout.buildDirectory
                        .dir("target/staging-deploy")
                        .get()
                        .toString(),
                )
                username = getRepositoryUsername()
            }
        }
    }
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
        fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
            exclude("**/*")

            include("**/io/legere/pdfiumandroid/jni/**/*.class")
            // Adjust path if needed
        },
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            // For Kotlin classes
            include("**/io/legere/pdfiumandroid/jni/**/*.class")
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
