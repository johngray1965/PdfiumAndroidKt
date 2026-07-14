/*
 * Copyright (c) 2025.  Legere. All rights reserved.
 */

package io.legere.support

import com.android.build.api.dsl.CommonExtension

internal fun configurePackaging(commonExtension: CommonExtension) {
    commonExtension.apply {
        packaging.apply {
            resources {
                excludes +=
                    setOf(
                        "META-INF/DEPENDENCIES",
                        "META-INF/LICENSE",
                        "META-INF/LICENSE.txt",
                        "META-INF/license.txt",
                        "META-INF/LICENSE.md",
                        "META-INF/NOTICE",
                        "META-INF/NOTICE.txt",
                        "META-INF/notice.txt",
                        "META-INF/LICENSE-notice.md",
                        "META-INF/ASL2.0",
                    )
            }
        }
    }
}
