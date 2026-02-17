import com.android.build.api.dsl.LibraryExtension


plugins {
    id("io.legere.convention.library")
    id("io.legere.convention.static.analysis")
    id("io.legere.convention.kover")
    id("io.legere.convention.publish")
//    id("org.jetbrains.dokka")
//    id("org.jetbrains.dokka-javadoc")
}

configure<LibraryExtension> {
    namespace = "io.legere.pdfiumandroid"

    defaultConfig {
        minSdk = 24
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
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            enableAndroidTestCoverage = true
        }
//        maybeCreate("qa")
//        getByName("qa") {
//            matchingFallbacks += listOf("release")
//            isMinifyEnabled = true
//            signingConfig = signingConfigs.getByName("debug")
//        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_21)
        targetCompatibility(JavaVersion.VERSION_21)
    }
    testCoverage {
        jacocoVersion = "0.8.13"
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
//    dokkaPlugin(libs.android.documentation.plugin)

//    dokka(project(":pdfiumandroid"))
//    dokka(project(":pdfiumandroid:api"))

    api(project(":pdfiumandroid:api"))
    implementation(project(":pdfiumandroid:core"))
    implementation(libs.guava)

    testImplementation(libs.junit)
    testImplementation(libs.espresso.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.core.testing)
    testImplementation(libs.bundles.test)
    testImplementation(libs.ext.junit)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.vintage.engine)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    kover(project(":pdfiumandroid:arrow"))
    kover(project(":pdfiumandroid:api"))
}
kover {
    reports {
        // filters for all report types of all build variants
        filters {
            excludes {
//                androidGeneratedClasses()
//                packages(
//                    "io.legere.pdfiumandroid.core.jni",
//                )
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
                    "io.legere.pdfiumandroid.core.unlocked.SystemLibraryLoader",
                    "io.legere.pdfiumandroid.api.LockManagerSplitLock",
                    "io.legere.pdfiumandroid.api.LockManagerSuspendOnly",
                    "io.legere.pdfiumandroid.api.LockManagerSuspendWithBlocking",
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

publishPlugin {
    artifactId.set("pdfiumandroid")
    name.set("pdfiumandroid")
    description.set(rootProject.properties["POM_DESCRIPTION"] as String)
}

// dokka {
//    dokkaPublications.html {
// //        moduleName.set(project.name)
// //        moduleVersion.set(project.version.toString())
//        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
//        failOnWarning.set(false)
//        suppressInheritedMembers.set(false)
//        suppressObviousFunctions.set(true)
//        offlineMode.set(true)
//        Configuring
//    }
//    dokkaSourceSets {
// //        named("debug") {
// //            sourceRoots.from(file("src/main/java"))
// //        }
//        configureEach {
//            println("Configuring $name")
//            suppress.set(false)
//            displayName.set(name)
//            documentedVisibilities.set(setOf(VisibilityModifier.Public))
//            reportUndocumented.set(false)
//            skipEmptyPackages.set(true)
//            skipDeprecated.set(false)
//            suppressGeneratedFiles.set(true)
//        }
//    }
// }

// dokka {
//    dokkaPublications.html {
// //        moduleName.set(project.name) // Sets the module name in the docs navigation
// //        moduleVersion.set(project.version.toString()) // Optional: sets the version
// //
// //        // ***** IMPORTANT: DO NOT set outputDirectory.set(...) here in submodules. *****
// //        // The root Dokka task will handle the overall aggregated output directory.
// //
// //        failOnWarning.set(false)
// //        suppressInheritedMembers.set(false)
// //        suppressObviousFunctions.set(true)
// //        offlineMode.set(true)
//
//        // Include Markdown files if you have them for this module
//        includes.from(
//            fileTree("src/main/java") {
//                include("**/*.kt")
//            },
//        )
//
//        // Configure source sets *within* this publication for module-specific exclusions
// //        dokkaSourceSets {
// //            named("main") {
// //                // Exclude BuildConfig.kt from this module's documentation
// // //                exclude("**/BuildConfig.kt")
// //                // Add other exclusion patterns here if needed
// //            }
// //            named("test") { suppress.set(true) } // Example: suppress test docs
// //        }
//    }
// //    dokkaSourceSets {
// //        // Example: Configuration exclusive to the 'linux' source set
// //
// //        configureEach {
// //            suppress.set(false)
// //            displayName.set(name)
// //            documentedVisibilities.set(setOf(VisibilityModifier.Public))
// //            reportUndocumented.set(false)
// //            skipEmptyPackages.set(true)
// //            skipDeprecated.set(false)
// //            suppressGeneratedFiles.set(true)
// // //            jdkVersion.set(8)
// // //            languageVersion.set("1.7")
// // //            apiVersion.set("1.7")
// //            sourceRoots.from(file("src"))
// // //            classpath.from(file("libs/dependency.jar"))
// // //            samples.from("samples/Basic.kt", "samples/Advanced.kt")
// //
// //            sourceLink {
// //                localDirectory.set(file("src/main/java"))
// //                remoteUrl("https://example.com/src")
// //                remoteLineSuffix.set("#L")
// //            }
// //
// // //            externalDocumentationLinks {
// // //                url = URL("https://example.com/docs/")
// // //                packageListUrl = File("/path/to/package-list").toURI().toURL()
// // //            }
// //
// //            perPackageOption {
// // //                matchingRegex.set(".*api.*")
// //                suppress.set(false)
// //                skipDeprecated.set(false)
// //                reportUndocumented.set(false)
// //                documentedVisibilities.set(
// //                    setOf(
// //                        VisibilityModifier.Public,
// // //                        VisibilityModifier.Private,
// // //                        VisibilityModifier.Protected,
// // //                        VisibilityModifier.Internal,
// // //                        VisibilityModifier.Package,
// //                    ),
// //                )
// //            }
// //        }
// //    }
// }
