package io.legere.convention

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class KoverPlugin : Plugin<Project> {
    private val koverPluginId = "org.jetbrains.kotlinx.kover"

    @Suppress("LongMethod")
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(koverPluginId)
            }
            extensions.configure<KoverProjectExtension> {
//                useJacoco("0.8.14")
                reports {
                    // filters for all report types of all build variants
                    filters {
                        excludes {
//                androidGeneratedClasses()
                            inheritedFrom("dagger.MembersInjector")
                            inheritedFrom("dagger.hilt.internal.GeneratedComponentManagerHolder")
                            inheritedFrom("dagger.internal.Factory")
                            packages(
                                "dagger.hilt.internal.aggregatedroot.codegen",
                                "hilt_aggregated_deps",
                            )
                            annotatedBy(
                                // compose preview
                                "androidx.compose.ui.tooling.preview.Preview",
                                // begin Hilt classes
                                "javax.annotation.processing.Generated",
                                "dagger.internal.DaggerGenerated",
                                "dagger.hilt.android.internal.lifecycle.HiltViewModelMap\$KeySet",
                                // end Hilt classes
                                "kotlinx.serialization.SerialName",
                            )
                            classes(
                                // begin excludes generated classes
                                "*.databinding.*",
                                "*.BuildConfig",
                                // end excludes generated classes
                                // begin Hilt classes
                                "*\$InstanceHolder",
                                "*Hilt_*",
                                "*BindsModule*",
                                "*\$DefaultImpls",
                                "*_HiltModules",
                                "*_HiltModules$*",
                                "*_HiltComponents",
                                // end Hilt classes
                                // excludes debug classes
                                // The Compose Text Fragment which isn't in use yet
                                "*.DebugUtil",
                                "*.*Args*",
                                "*.*Directions*",
                                "*.*Args\$Companion",
                                "*.*Directions\$Companion",
                                "*.ComposableSingletons*",
                                "androidx.navigation.ui.AppBarConfigurationKt",
                            )
                        }
                    }
                }
            }

            tasks.withType<Test>().configureEach {
                jvmArgs(
                    "--add-opens",
                    "java.base/java.lang=ALL-UNNAMED",
                    "--add-opens",
                    "java.base/java.lang.reflect=ALL-UNNAMED",
                    "--add-opens",
                    "java.base/java.util=ALL-UNNAMED",
                    "--add-opens",
                    "java.base/java.util.concurrent=ALL-UNNAMED",
                )
            }
        }
    }
}
