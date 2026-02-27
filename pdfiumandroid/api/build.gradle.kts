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
