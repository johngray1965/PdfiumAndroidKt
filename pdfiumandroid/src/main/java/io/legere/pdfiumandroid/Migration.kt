/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused", "EXTENSION_SHADOWED_BY_MEMBER")

package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.suspend.PdfPageKt
import io.legere.pdfiumandroid.suspend.PdfTextPageKt
import io.legere.pdfiumandroid.api.Logger as NewLogger
import io.legere.pdfiumandroid.api.PdfPasswordException as NewPdfPasswordException
import io.legere.pdfiumandroid.api.PdfWriteCallback as NewPdfWriteCallback
import io.legere.pdfiumandroid.api.PdfiumSource as NewPdfiumSource
import io.legere.pdfiumandroid.api.WordRangeRect as NewWordRangeRect

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.Logger",
    ReplaceWith("Logger", "io.legere.pdfiumandroid.api.Logger"),
)
typealias Logger = NewLogger

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.PdfPasswordException",
    ReplaceWith(
        "PdfPasswordException",
        "io.legere.pdfiumandroid.api.PdfPasswordException",
    ),
)
typealias PdfPasswordException = NewPdfPasswordException

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.PdfWriteCallback",
    ReplaceWith(
        "PdfWriteCallback",
        "io.legere.pdfiumandroid.api.PdfWriteCallback",
    ),
)
typealias PdfWriteCallback = NewPdfWriteCallback

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.PdfiumSource",
    ReplaceWith("PdfiumSource", "io.legere.pdfiumandroid.api.PdfiumSource"),
)
typealias PdfiumSource = NewPdfiumSource

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.WordRangeRect",
    ReplaceWith("WordRangeRect", "io.legere.pdfiumandroid.api.WordRangeRect"),
)
typealias WordRangeRect = NewWordRangeRect

/**
 * Wrapper to support legacy .page.pageIndex access in migration.
 * This class allows existing code using `textPage.page.pageIndex` to compile
 * (with deprecation warnings) by replacing `.page` with a call that returns this wrapper,
 * which exposes `pageIndex`.
 */
class LegacyPageWrapper(
    val pageIndex: Int,
)

@Deprecated("Use pageIndex directly", ReplaceWith("this"))
val PdfPage.page: LegacyPageWrapper get() = LegacyPageWrapper(this.pageIndex)

@Deprecated("Use pageIndex directly", ReplaceWith("this"))
val PdfTextPage.page: LegacyPageWrapper get() = LegacyPageWrapper(this.pageIndex)

@Deprecated("Use pageIndex directly", ReplaceWith("this"))
val PdfPageKt.page: LegacyPageWrapper get() = LegacyPageWrapper(this.pageIndex)

@Deprecated("Use pageIndex directly", ReplaceWith("this"))
val PdfTextPageKt.page: LegacyPageWrapper get() = LegacyPageWrapper(this.pageIndex)
