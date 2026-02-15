/*
 * Copyright (c) 2025.  Legere. All rights reserved.
 */

@file:Suppress("UNCHECKED_CAST")

package io.legere.support

import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.NamedDomainObjectContainer

internal fun configureBuildTypes(commonExtension: CommonExtension) {
    if (commonExtension is LibraryExtension) {
        commonExtension.defaultConfig {
            consumerProguardFiles("consumer-rules.pro")
        }
    }

    val buildTypes = commonExtension.buildTypes as NamedDomainObjectContainer<BuildType>
    with(buildTypes) {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        maybeCreate("qa")
        getByName("qa") {
            isMinifyEnabled = false
            proguardFiles(
                commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            commonExtension.signingConfigs.maybeCreate("qa").initWith(commonExtension.signingConfigs.getByName("debug"))

            matchingFallbacks += listOf("release")
        }
        maybeCreate("benchmark")
        getByName("benchmark") {
            initWith(getByName("release"))
            commonExtension.signingConfigs.maybeCreate("benchmark").initWith(commonExtension.signingConfigs.getByName("debug"))
            matchingFallbacks += listOf("release")
        }
    }
}
