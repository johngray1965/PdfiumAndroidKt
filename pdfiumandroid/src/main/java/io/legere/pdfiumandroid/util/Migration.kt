@file:Suppress("unused")

package io.legere.pdfiumandroid.util

import io.legere.pdfiumandroid.api.Config as NewConfig
import io.legere.pdfiumandroid.api.Size as NewSize

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.Size",
    ReplaceWith("io.legere.pdfiumandroid.api.Size", "io.legere.pdfiumandroid.api.Size"),
)
typealias Size = NewSize

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.Config",
    ReplaceWith("io.legere.pdfiumandroid.api.Config", "io.legere.pdfiumandroid.api.Config"),
)
typealias Config = NewConfig
