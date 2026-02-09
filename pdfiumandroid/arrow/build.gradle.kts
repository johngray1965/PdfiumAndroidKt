import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.gradle.publish)
    jacoco
    `maven-publish`
    signing
}
jacoco {
    toolVersion = "0.8.13"
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

configure<LibraryExtension> {
    namespace = "io.legere.pdfiumandroid.arrow"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        testOptions {
            animationsDisabled = true
            unitTests {
                isIncludeAndroidResources = true
                isReturnDefaultValues = true
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_21)
        targetCompatibility(JavaVersion.VERSION_21)
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
    implementation(project(":pdfiumandroid"))
    compileOnly(libs.arrow.core)
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
    testImplementation(libs.arrow.fx.coroutines)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.core.testing)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.legere"
            artifactId = "pdfium-android-kt-arrow"
            version = project.property("VERSION_NAME") as String

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
    projectDescription.set("Arrow support for PdfiumAndroid")
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
//    repository("https://maven.pkg.github.com/OWNER/REPOSITORY", "GitHub") {
//        user.set(System.getenv("GITHUB_USERNAME"))
//        password.set(System.getenv("GITHUB_TOKEN"))
//    }
}
