package io.legere.convention

import com.android.build.api.dsl.LibraryExtension
import io.legere.support.common
import io.legere.support.configureBuildTypes
import io.legere.support.configureKotlinAndroid
import io.legere.support.configureLint
import io.legere.support.configurePackaging
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
//        val androidLibraryPublishMetaData =
//            project.extensions.create(
//                "androidLibraryPublishMetaData",
//                AndroidLibraryPublishMetaData::class.java
//            )

        with(project) {
            with(pluginManager) {
                apply("com.android.library")
//                apply("maven-publish")
            }

            extensions.configure<LibraryExtension> {
//                defaultConfig.targetSdk = AppConfig.targetSdk
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                defaultConfig {
                    testOptions {
                        animationsDisabled = true
                        unitTests {
                            isIncludeAndroidResources = true
                            isReturnDefaultValues = true
                        }
                    }
                }
                common()
                configureBuildTypes(this)
                configureLint(this)
                configurePackaging(this)
                configureKotlinAndroid(this)
                buildFeatures {
                    buildConfig = true
                }
                testOptions {
                    unitTests.all {
                        it.useJUnitPlatform()
                    }
                }
            }

//            project.afterEvaluate {
//                configureAndroidLibraryPublish(androidLibraryPublishMetaData)
//            }

            dependencies {
                // Add common dependencies here (example testing library)
            }
        }
    }
}

// internal fun Project.configureAndroidLibraryPublish(metaData: AndroidLibraryPublishMetaData) {
//    if (!metaData.isPublishEnabled) return
//
//    afterEvaluate {
//        val allVariants = (extensions.getByName("android") as LibraryExtension).libraryVariants.map { it.name }.toSet()
//        val eligiblePublication = metaData.publicationList.filter { allVariants.contains(it.variantName) }
//
//        val publishing = extensions.getByType(PublishingExtension::class.java) // from maven-publish plugin
//
//        eligiblePublication.forEach { publication ->
//            publishing.publications.create(publication.variantName, MavenPublication::class.java) {
//                groupId = publication.groupId
//                artifactId = publication.artifactId
//                version = publication.version
//
//                from(components.getAt(publication.variantName))
//
//                pom {
//                    name.set(publication.name)
//                    description.set(publication.description)
//                }
//            }
//        }
//    }
// }
//
// abstract class AndroidLibraryPublishMetaData {
//    var isPublishEnabled = true
//    var publicationList: List<PublicationDetail> = emptyList()
// }
//
// data class PublicationDetail(
//    val variantName: String,
//    val groupId: String,
//    val artifactId: String,
//    val version: String,
//    val name: String? = null,
//    val description: String? = null
// )
