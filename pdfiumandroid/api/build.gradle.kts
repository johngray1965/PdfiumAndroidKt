import com.android.build.api.dsl.LibraryExtension

plugins {
    id("io.legere.convention.library")
    id("io.legere.convention.static.analysis")
    id("io.legere.convention.kover")
    alias(libs.plugins.gradle.publish)
    `maven-publish`
    signing

//    id("org.jetbrains.dokka")
//    id("org.jetbrains.dokka-javadoc")
}

configure<LibraryExtension> {
    namespace = "io.legere.pdfiumandroid.api"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
//    dokkaPlugin(libs.android.documentation.plugin)
    implementation(libs.androidx.core.ktx)
    implementation(libs.guava)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.legere"
            artifactId = "pdfiumandroid-core"
            version = project.property("VERSION_NAME") as String

            pom {
                name.set("pdfiumandroid.core")
                description.set("Core APIs for PdfiumAndroid")
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
