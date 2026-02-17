@file:Suppress("unused")

package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.api.Logger as NewLogger
import io.legere.pdfiumandroid.api.PdfPasswordException as NewPdfPasswordException
import io.legere.pdfiumandroid.api.PdfWriteCallback as NewPdfWriteCallback
import io.legere.pdfiumandroid.api.PdfiumSource as NewPdfiumSource

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.Logger",
    ReplaceWith("io.legere.pdfiumandroid.api.Logger", "io.legere.pdfiumandroid.api.Logger"),
)
typealias Logger = NewLogger

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.PdfPasswordException",
    ReplaceWith(
        "io.legere.pdfiumandroid.api.PdfPasswordException",
        "io.legere.pdfiumandroid.api.PdfPasswordException",
    ),
)
typealias PdfPasswordException = NewPdfPasswordException

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.PdfWriteCallback",
    ReplaceWith(
        "io.legere.pdfiumandroid.api.PdfWriteCallback",
        "io.legere.pdfiumandroid.api.PdfWriteCallback",
    ),
)
typealias PdfWriteCallback = NewPdfWriteCallback

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.PdfiumSource",
    ReplaceWith("io.legere.pdfiumandroid.api.PdfiumSource", "io.legere.pdfiumandroid.api.PdfiumSource"),
)
typealias PdfiumSource = NewPdfiumSource
