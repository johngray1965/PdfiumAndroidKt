


plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    `maven-publish`
}

android {
    namespace = "io.legere.pdfiumandroid.arrow"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("io.legere:pdfiumandroid:1.0.17")
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.arrow.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.arrow.fx.coroutines)
}

fun isReleaseBuild(): Boolean = !findProject("VERSION_NAME").toString().contains("SNAPSHOT")

fun getReleaseRepositoryUrl(): String {
    return if (rootProject.hasProperty("RELEASE_REPOSITORY_URL")) {
        rootProject.properties["RELEASE_REPOSITORY_URL"] as String
    } else {
        "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    }
}

fun getSnapshotRepositoryUrl(): String {
    return if (rootProject.hasProperty("SNAPSHOT_REPOSITORY_URL")) {
        rootProject.properties["SNAPSHOT_REPOSITORY_URL"] as String
    } else {
        "https://oss.sonatype.org/content/repositories/snapshots/"
    }
}

fun getRepositoryUrl(): String {
    return if (isReleaseBuild()) getReleaseRepositoryUrl() else getSnapshotRepositoryUrl()
}

fun getRepositoryUsername(): String {
    return if (rootProject.hasProperty("NEXUS_USERNAME")) rootProject.properties["NEXUS_USERNAME"] as String else ""
}

fun getRepositoryPassword(): String {
    return if (rootProject.hasProperty("NEXUS_PASSWORD")) rootProject.properties["NEXUS_PASSWORD"] as String else ""
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.legere"
            artifactId = "pdfiumandroid-arrow"
            version = rootProject.properties["VERSION_NAME"] as String

            pom {
                name.set("PdfiumAndroid.Arrow")
                description.set("Arrow support for PdfiumAndroid")
                url.set(rootProject.properties["POM_URL"] as String)
                licenses {
                    license {
                        name.set(rootProject.properties["POM_LICENCE_NAME"] as String)
                        url.set(rootProject.properties["POM_LICENCE_URL"] as String)
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
