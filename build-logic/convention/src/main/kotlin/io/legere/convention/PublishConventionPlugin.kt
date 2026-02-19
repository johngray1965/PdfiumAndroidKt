package io.legere.convention

import com.android.build.api.dsl.LibraryExtension
import org.danilopianini.gradle.mavencentral.PublishOnCentralExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension

class PublishConventionPlugin : Plugin<Project> {
    //    gradle-publish = { id = "org.danilopianini.publish-on-central", version.ref = "publish_on_central"}
    override fun apply(project: Project) {
        val publishPluginExtension =
            project.extensions.create<PublishPluginExtension>("publishPlugin")
        with(project) {
            with(pluginManager) {
                apply("org.danilopianini.publish-on-central")
                apply("maven-publish")
                apply("signing")
            }

            extensions.configure<LibraryExtension> {
                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                        withJavadocJar()
                    }
                }
            }

            configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("maven") {
                        groupId = "io.legere"

                        // Resolve version robustly
                        val ver = project.version.toString()
                        val finalVersion =
                            if (ver == "unspecified") {
                                val rootVer = project.rootProject.version.toString()
                                if (rootVer == "unspecified") {
                                    error(
                                        "Project version is 'unspecified'. Check git-sensitive-semantic-versioning configuration or tags.",
                                    )
                                }
                                rootVer
                            } else {
                                ver
                            }
                        version = finalVersion

                        pom {
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
                        project.afterEvaluate {
                            artifactId = publishPluginExtension.artifactId.get()
                            pom {
                                name.set(publishPluginExtension.name.get())
                                description.set(publishPluginExtension.description.get())
                            }
                            from(components["release"])
                        }
                    }
                }
                repositories {
                    maven {
                        url = uri(layout.buildDirectory.dir("target/staging-deploy"))
                    }
                }
            }

            extensions.configure<SigningExtension> {
                val keyId = project.properties["signing.keyId"]?.toString() ?: ""
                val password = project.properties["signing.password"]?.toString() ?: ""

                // This attempts to read the keyring file specified in gradle.properties
                // which contains the full PGP key material.
                if (keyId.isNotBlank() && password.isNotBlank()) {
                    configure<PublishingExtension> {
                        publications.withType<MavenPublication> {
                            sign(this)
                        }
                    }
                }
            }
            extensions.configure<PublishOnCentralExtension> {
                repoOwner.set("johngray1965") // Used to populate the default value for projectUrl and scmConnection
                projectDescription.set(rootProject.properties["POM_DESCRIPTION"] as String)
                // The following values are the default, if they are ok with you, just omit them
                projectLongName.set(project.name)
                licenseName.set("Apache License, Version 2.0")
                licenseUrl.set("http://www.apache.org/licenses/LICENSE-2.0")
                projectUrl.set("https://github.com/${repoOwner.get()}/${project.name}")
                scmConnection.set("scm:git:https://github.com/${repoOwner.get()}/${project.name}")

                repository("https://maven.pkg.github.com/johngray1965/PdfiumAndroidKt", "GitHub") {
                    user.set(System.getenv("GITHUB_USERNAME") ?: project.findProperty("GITHUB_USERNAME") as? String ?: "")
                    password.set(System.getenv("GITHUB_TOKEN") ?: project.findProperty("GITHUB_TOKEN") as? String ?: "")
                }
            }
        }
    }
}
