/*
 * Copyright (c) 2025.  Legere. All rights reserved.
 */

package io.legere.support

import java.math.BigDecimal

@Suppress("ktlint:standard:property-naming")
object AppConfig {
    const val compileSdk: Int = 36
    const val minSdk: Int = 24
    const val targetSdk: Int = 36

    val minimumCoverageLimit: BigDecimal = BigDecimal("0.7")
}
