import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jreleaser.model.Active
import org.jreleaser.model.Signing


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.jreleaser)
    `maven-publish`
    signing
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "io.legere.pdfiumandroid.arrow"
    compileSdk = 35

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }
}

dependencies {
    implementation(project(":pdfiumandroid"))
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
    if (rootProject.hasProperty("JRELEASER_MAVENCENTRAL_USERNAME")) {
        rootProject.properties["JRELEASER_MAVENCENTRAL_USERNAME"] as String
    } else {
        ""
    }

fun getRepositoryPassword(): String =
    if (rootProject.hasProperty("JRELEASER_MAVENCENTRAL_TOKEN")) {
        rootProject.properties["JRELEASER_MAVENCENTRAL_TOKEN"] as String
    } else {
        ""
    }

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.legere"
            artifactId = "pdfium-android-kt-arrow"
            version = "1.0.25"

            pom {
                name.set("pdfiumandroid.arrow")
                description.set("Arrow support for PdfiumAndroid")
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
        description = "Arrow support for PdfiumAndroid"
        version = rootProject.properties["VERSION_NAME"] as String
        license = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        links {
            homepage = "https://github.com/johngray1965/PdfiumAndroidKt"
            license = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        }
    }
    gitRootSearch = true
    signing {
        active = Active.ALWAYS
        mode = Signing.Mode.COMMAND
        armored = true
        verify = false
        command {
            executable = "gpg"
            keyName = "4BBF8FAB"
            publicKeyring = "/Users/gray/.gnupg/secring.gpg"
        }
    }
    release {
        github {
            skipRelease = true
        }
    }
//    distributions {
//        create("pdfiumandroid.arrow") {
//            artifact {
//                path = file("build/distributions/{{distributionName}}-{{projectVersion}}.zip")
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
