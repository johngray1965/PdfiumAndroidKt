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
                            inheritedFrom("com.voicedream.reader.ui.BaseComposeFragment")
                            packages(
                                "dagger.hilt.internal.aggregatedroot.codegen",
                                "hilt_aggregated_deps",
                                "io.legere.room.di",
                                "com.voicedream.di",
                                "com.voicedream.reader.di",
                                "io.legere.readerdata.di",
                                "io.legere.readerdomain.di",
                                "com.android.vending.billing",
                                "com.voicedream.reader.ui.navigation",
                                // Things we can't test without device, kover doesn't support it
                                "com.voicedream.readerservice.docreader.util",
                                "io.legere.readerdata.content.loader.pdf",
                                "io.legere.readerdata.content.loader.image",
                                "com.voicedream.engine.systemtts",
                                "io.legere.readerdata.contracts.encrypt",
                                "com.voicedream.reader.ui.reader.pdf",
                                "io.legere.sharedComposeUI.text",
                                "com.voicedream.scanner.di",
                                "com.voicedream.scanner.ui.navigation",
                                "com.voicedream.scanner.data.serializers",
                                "com.voicedream.scanner.data.serialiazable",
                                "com.voicedream.scanner.ui.extensions",
                                "com.voicedream.scanner.ui.resources",
                                "com.voicedream.scanner.data.imageprocessing",
                                "com.voicedream.scanner.usecases.android",
                                "com.voicedream.scanner.util.android"
                            )
                            annotatedBy(
                                // compose preview
                                "androidx.compose.ui.tooling.preview.Preview",
                                // begin Hilt classes
                                "javax.annotation.processing.Generated",
                                "dagger.internal.DaggerGenerated",
                                "dagger.hilt.android.internal.lifecycle.HiltViewModelMap\$KeySet",
                                // end Hilt classes
                                "kotlinx.serialization.SerialName"
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
                                "com.voicedream.reader.HiltTestActivity",
                                // end Hilt classes
                                // excludes debug classes
                                // The Compose Text Fragment which isn't in use yet
                                "com.voicedream.reader.ui.reader.text.ComposeTextDocFragmentKt*",
                                "com.voicedream.reader.ui.reader.text.ScrollPhase*",
                                "com.voicedream.reader.ui.reader.text.StoredHandleLocalInfo*",
                                "com.voicedream.reader.ui.reader.text.TwoFingerTapDetectorKt*",
                                "com.voicedream.reader.ReaderApplication", // The application classes aren't used in the tests
                                "com.voicedream.reader.ReaderApplication*", // The application classes aren't used in the tests
                                "com.voicedream.reader.ReaderApplicationKt", // The application classes aren't used in the tests
                                "com.voicedream.reader.BaseApplication", // The application classes aren't used in the tests
                                "com.voicedream.reader.BaseApplication*", // The application classes aren't used in the tests
                                "com.voicedream.reader.LegereDebugTree",
                                "com.voicedream.reader.OutOfMemoryDumper",
                                "com.voicedream.reader.OutOfMemoryDumper\$Companion",
                                "com.voicedream.reader.ui.BaseActivity",
                                "io.legere.readerdata.data.backup.Document3188",
                                "io.legere.room.ContentProvideBaseConverter", // We can no longer get back there
                                "io.legere.room.ContentProvideBaseConverter*", // We can no longer get back there
                                "io.legere.room.VoiceDreamDatabase_Impl", // Generated by Room
                                "io.legere.room.VoiceDreamDatabase_Impl*", // Generated by Room
                                "io.legere.readerdata.content.loader.html.Readability4JPlugin", // not our code
                                "com.voicedream.reader.contracts.auth.AuthResultContract",
                                "com.voicedream.reader.contracts.generic.ActivityResultObserverDefaultImpl",
                                "com.voicedream.reader.ui.contentsources.bookshare.BookshareCategories",
                                "com.bumptech.glide.*",
                                "com.voicedream.reader.ui.docview.AudioViewFragment", // just hosts compose, compose has tests"
                                "com.voicedream.reader.ui.docview.AudioViewFragment$*", // just hosts compose, compose has tests"
                                "io.legere.readerdata.data.util.CryptoManager", // currently unused
                                "io.legere.readerdata.util.ImportUtil", // currently unused
                                "com.voicedream.reader.ui.library.ComposableSingletons*",
                                "com.voicedream.reader.ui.settings.readerscreen.ComposableSingletons*",
                                "io.legere.room.entities.Folder",
                                "io.legere.room.ContentProvideBaseConverter",
//                    "io.legere.room.dao.*_Impl",
//                    "io.legere.room.dao.*_Impl$*",
                                "name.fraser.neil.plaintext.*",
                                "*.DebugUtil",
                                "*.*Args*",
                                "*.*Directions*",
                                "*.*Args\$Companion",
                                "*.*Directions\$Companion",
                                "*.ComposableSingletons*",
                                "androidx.navigation.ui.AppBarConfigurationKt",
                                // scanner
                                "com.voicedream.scanner.data.room.ScannerDatabase_Impl",
                                "com.voicedream.scanner.data.room.ScannerDatabase_Impl$*",
                                "com.voicedream.scanner.App",
                                "com.voicedream.scanner.App$*",
                                "com.voicedream.scanner.ui.MainActivity",
                                "com.voicedream.scanner.ui.MainActivity$*",
                                "com.voicedream.scanner.LegereDebugTree",

                            )
                        }
                    }
                }
            }
            
            tasks.withType<Test>().configureEach {
                jvmArgs(
                    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                    "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
                    "--add-opens", "java.base/java.util=ALL-UNNAMED",
                    "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED"
                )
            }
        }
    }
}
