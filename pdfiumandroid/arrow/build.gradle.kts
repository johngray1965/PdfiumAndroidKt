/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023-2026 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.android.build.api.dsl.LibraryExtension


plugins {
    id("io.legere.convention.library")
    id("io.legere.convention.static.analysis")
    id("io.legere.convention.kover")
    id("io.legere.convention.junit5")
    id("io.legere.convention.publish")
//    id("org.jetbrains.dokka")
//    id("org.jetbrains.dokka-javadoc")
    jacoco
}
jacoco {
    toolVersion = "0.8.13"
}

configure<LibraryExtension> {
    namespace = "io.legere.pdfiumandroid.arrow"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
//    dokkaPlugin(libs.android.documentation.plugin)
//    dokka(project(":pdfiumandroid:api:"))
    implementation(project(":pdfiumandroid"))
    api(project(":pdfiumandroid:api"))
    implementation(project(":pdfiumandroid:core"))

    compileOnly(libs.arrow.core)
    compileOnly(libs.kotlinx.coroutines.android)
    compileOnly(libs.androidx.annotation.jvm)
    compileOnly(libs.kotlin.stdlib)
    implementation(libs.guava)

    testImplementation(libs.junit)
    testImplementation(libs.espresso.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.core.testing)
    testImplementation(libs.bundles.test)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.vintage.engine)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.arrow.fx.coroutines)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.core.testing)
}

publishPlugin {
    artifactId.set("pdfium-android-kt-arrow")
    name.set("pdfiumandroid.arrow")
    description.set("Arrow support for PdfiumAndroid")
}
