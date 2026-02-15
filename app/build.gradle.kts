import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("io.legere.convention.application.compose")
    id("io.legere.convention.hilt")
    id("io.legere.convention.static.analysis")
    id("io.legere.convention.kover")
}

configure<ApplicationExtension> {
    namespace = "io.legere.pdfiumandroidkt"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.legere.pdfiumandroidkt"
        minSdk = 24
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
//        maybeCreate("qa")
//        getByName("qa") {
//            matchingFallbacks += listOf("release")
//            isMinifyEnabled = true
//            signingConfig = signingConfigs.getByName("debug")
//            matchingFallbacks += listOf("release")
//        }
    }
}
configurations {
    configureEach {
        exclude(module = "httpclient")
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(project(":pdfiumandroid"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.coil.compose)
    implementation(libs.compose.glide)

    implementation(libs.timber)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
}

// allprojects {
//    tasks.withType(KotlinCompile).configureEach {
//        kotlinOptions {
//            jvmTarget = "1.8"
//        }
//    }
// }
