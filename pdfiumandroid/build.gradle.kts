import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    `maven-publish`
    signing
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xstring-concat=inline")
    }
}

android {
    namespace = "io.legere.pdfiumandroid"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        maybeCreate("qa")
        getByName("qa") {
            matchingFallbacks += listOf("release")
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
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

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.annotation.jvm)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.core.testing)
}

fun isReleaseBuild(): Boolean = !findProject("VERSION_NAME").toString().contains("SNAPSHOT")

fun getReleaseRepositoryUrl(): String =
    if (rootProject.hasProperty("RELEASE_REPOSITORY_URL")) {
        rootProject.properties["RELEASE_REPOSITORY_URL"] as String
    } else {
        "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    }

fun getSnapshotRepositoryUrl(): String =
    if (rootProject.hasProperty("SNAPSHOT_REPOSITORY_URL")) {
        rootProject.properties["SNAPSHOT_REPOSITORY_URL"] as String
    } else {
        "https://oss.sonatype.org/content/repositories/snapshots/"
    }

fun getRepositoryUrl(): String = if (isReleaseBuild()) getReleaseRepositoryUrl() else getSnapshotRepositoryUrl()

fun getRepositoryUsername(): String =
    if (rootProject.hasProperty("NEXUS_USERNAME")) rootProject.properties["NEXUS_USERNAME"] as String else ""

fun getRepositoryPassword(): String =
    if (rootProject.hasProperty("NEXUS_PASSWORD")) rootProject.properties["NEXUS_PASSWORD"] as String else ""

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = System.getenv("GROUP")
            artifactId = System.getenv("POM_ARTIFACT_ID")
            version = System.getenv("VERSION_NAME")

            pom {
                name.set("pdfiumandroid")
                packaging = rootProject.properties["POM_PACKAGING"] as String
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
            url = uri(getRepositoryUrl())
            credentials {
                username = getRepositoryUsername()
                password = getRepositoryPassword()
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

// fun isReleaseBuild(): Boolean {
//    val version = System.getenv("VERSION_NAME") ?: "1.0.0"
//    return version.contains("SNAPSHOT")
// }
//
// fun getReleaseRepositoryUrl(): String =
//    System.getenv("RELEASE_REPOSITORY_URL") ?: "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
//
// fun getSnapshotRepositoryUrl(): String =
//    System.getenv("SNAPSHOT_REPOSITORY_URL") ?: "https://oss.sonatype.org/content/repositories/snapshots/"
//
// fun getRepositoryUrl(): String = if (isReleaseBuild()) getReleaseRepositoryUrl() else getSnapshotRepositoryUrl()
//
// fun getRepositoryUsername(): String = System.getenv("NEXUS_USERNAME") ?: ""
//
// fun getRepositoryPassword(): String = System.getenv("NEXUS_PASSWORD") ?: ""
// publishing {
//    publications {
//        create<MavenPublication>("release") {
//            groupId = System.getenv("GROUP")
//            artifactId = System.getenv("POM_ARTIFACT_ID")
//            version = System.getenv("VERSION_NAME")
//            pom {
//
//                name = "pdfiumandroid"
//                packaging = System.getenv("POM_PACKAGING")
//                description = System.getenv("POM_DESCRIPTION")
//                url = System.getenv("POM_URL")
//
//                licenses {
//                    license {
//                        name = System.getenv("POM_LICENCE_NAME")
//                        url = System.getenv("POM_LICENCE_URL")
//                        distribution = System.getenv("POM_LICENCE_DIST")
//                    }
//                }
//                developers {
//                    developer {
//                        id = System.getenv("POM_DEVELOPER_ID")
//                        name = System.getenv("POM_DEVELOPER_NAME")
//                    }
//                }
//                scm {
//                    url = System.getenv("POM_SCM_URL")
//                    connection = System.getenv("POM_SCM_CONNECTION")
//                    developerConnection = System.getenv("POM_SCM_DEV_CONNECTION")
//                }
//            }
//
//            afterEvaluate {
//                from(components["release"])
//            }
//        }
//    }
//    repositories {
//        maven {
//            url = uri(getRepositoryUrl())
//            credentials {
//                username = getRepositoryUsername() // project.findProperty("gpr.user") ?: System.getenv("GITHUB_USER")
//                password = getRepositoryPassword() // project.findProperty("gpr.key") ?: System.getenv("TOKEN")
//            }
//        }
//    }
// }
// signing {
//    sign(publishing.publications["release"])
// }
//
// // afterEvaluate {
// //    publishing {
// //        publications {
// //            mavenRelease(MavenPublication) {
// //                from(components["release"])
// //            }
// //        }
// //    }
// // }
