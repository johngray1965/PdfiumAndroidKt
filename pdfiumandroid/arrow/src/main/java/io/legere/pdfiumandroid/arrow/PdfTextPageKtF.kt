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

@file:Suppress("unused")

package io.legere.pdfiumandroid.arrow

import arrow.core.Either
import io.legere.pdfiumandroid.PdfTextPage
import io.legere.pdfiumandroid.api.FindFlags
import io.legere.pdfiumandroid.api.WordRangeRect
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.core.util.wrapLock
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

/**
 * PdfTextPageKtF represents a single text page of a PDF file.
 * @property page the [PdfTextPage] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 */
@Suppress("TooManyFunctions")
class PdfTextPageKtF internal constructor(
    internal val page: PdfTextPageU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    val pageIndex: Int
        get() = page.pageIndex

    /**
     * suspend version of [PdfTextPage.textPageCountChars]
     */
    suspend fun textPageCountChars(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            page.textPageCountChars()
        }

    /**
     * suspend version of [PdfTextPage.textPageGetText]
     */
    suspend fun textPageGetText(
        startIndex: Int,
        length: Int,
    ): Either<PdfiumKtFErrors, String?> =
        wrapEither(dispatcher) {
            page.textPageGetText(startIndex, length)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetUnicode]
     */
    suspend fun textPageGetUnicode(index: Int): Either<PdfiumKtFErrors, Char> =
        wrapEither(dispatcher) {
            page.textPageGetUnicode(index)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetCharBox]
     */
    suspend fun textPageGetCharBox(index: Int): Either<PdfiumKtFErrors, PdfRectF?> =
        wrapEither(dispatcher) {
            page.textPageGetCharBox(index)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetCharIndexAtPos]
     */
    suspend fun textPageGetCharIndexAtPos(
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double,
    ): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            page.textPageGetCharIndexAtPos(x, y, xTolerance, yTolerance)
        }

    /**
     * suspend version of [PdfTextPage.textPageCountRects]
     */
    suspend fun textPageCountRects(
        startIndex: Int,
        count: Int,
    ): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            page.textPageCountRects(startIndex, count)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetRect]
     */
    suspend fun textPageGetRect(rectIndex: Int): Either<PdfiumKtFErrors, PdfRectF?> =
        wrapEither(dispatcher) {
            page.textPageGetRect(rectIndex)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetRectsForRanges]
     */
    suspend fun textPageGetRectsForRanges(wordRanges: IntArray): Either<PdfiumKtFErrors, List<WordRangeRect>?> =
        wrapEither(dispatcher) {
            page.textPageGetRectsForRanges(wordRanges)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetBoundedText]
     */
    suspend fun textPageGetBoundedText(
        rect: PdfRectF,
        length: Int,
    ): Either<PdfiumKtFErrors, String?> =
        wrapEither(dispatcher) {
            page.textPageGetBoundedText(rect, length)
        }

    /**
     * suspend version of [PdfTextPage.getFontSize]
     */
    suspend fun getFontSize(charIndex: Int): Either<PdfiumKtFErrors, Double> =
        wrapEither(dispatcher) {
            page.getFontSize(charIndex)
        }

    suspend fun findStart(
        findWhat: String,
        flags: Set<FindFlags>,
        startIndex: Int,
    ): Either<PdfiumKtFErrors, FindResultKtF> =
        wrapEither(dispatcher) {
            val findResult = page.findStart(findWhat, flags, startIndex)
            if (findResult == null) {
                error("findResult is null")
            } else {
                FindResultKtF(findResult, dispatcher)
            }
        }

    suspend fun loadWebLink(): Either<PdfiumKtFErrors, PdfPageLinkKtF> =
        wrapEither(dispatcher) {
            page.loadWebLink()?.let {
                PdfPageLinkKtF(it, dispatcher)
            } ?: error("PdfPageLink is null")
        }

    /**
     * Close the page and free all resources.
     */
    override fun close() {
        wrapLock {
            page.close()
        }
    }

    fun safeClose(): Either<PdfiumKtFErrors, Boolean> =
        Either
            .catch {
                wrapLock {
                    page.close()
                }
                true
            }.mapLeft { exceptionToPdfiumKtFError(it) }
}
