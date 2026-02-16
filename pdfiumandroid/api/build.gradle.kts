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
    namespace = "io.legere.pdfiumandroid.api"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
publishPlugin {
    artifactId.set("pdfiumandroid-api")
    name.set("pdfiumandroid.api")
    description.set("Common APIs for PdfiumAndroid")
}
