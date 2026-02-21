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

@file:Suppress("unused", "CanBeVal")

package io.legere.pdfiumandroid.arrow

import android.view.Surface
import arrow.core.Either
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.api.Bookmark
import io.legere.pdfiumandroid.api.Meta
import io.legere.pdfiumandroid.api.PdfWriteCallback
import io.legere.pdfiumandroid.api.types.FloatRectValues
import io.legere.pdfiumandroid.api.types.MatrixValues
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.util.wrapLock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * PdfDocumentKtF represents a PDF file and allows you to load pages from it.
 * @property document the [PdfDocument] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 * @constructor create a [PdfDocumentKtF] from a [PdfDocument]
 */
@Suppress("TooManyFunctions", "CanBeVal")
class PdfDocumentKtF internal constructor(
    internal val document: PdfDocumentU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    /**
     *  suspend version of [PdfDocument.getPageCount]
     */
    suspend fun getPageCount(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            document.getPageCount()
        }

    /**
     *  suspend version of [PdfDocument.getPageCharCounts]
     */
    suspend fun getPageCharCounts(): Either<PdfiumKtFErrors, IntArray> =
        wrapEither(dispatcher) {
            document.getPageCharCounts()
        }

    /**
     * suspend version of [PdfDocument.openPage]
     */
    suspend fun openPage(pageIndex: Int): Either<PdfiumKtFErrors, PdfPageKtF> =
        wrapEither(dispatcher) {
            document.openPage(pageIndex)?.let {
                PdfPageKtF(it, dispatcher)
            } ?: error("Page is null")
        }

    /**
     * suspend version of [PdfDocument.openPages]
     */
    suspend fun openPages(
        fromIndex: Int,
        toIndex: Int,
    ): Either<PdfiumKtFErrors, List<PdfPageKtF>> =
        wrapEither(dispatcher) {
            document.openPages(fromIndex, toIndex).map { PdfPageKtF(it, dispatcher) }
        }

    /**
     * suspend version of [PdfDocument.renderPages]
     */
    @Suppress("LongParameterList", "ComplexMethod", "ComplexCondition")
    suspend fun renderPages(
        surface: Surface?,
        pages: List<PdfPageKtF>,
        matrices: List<MatrixValues>,
        clipRects: List<FloatRectValues>,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
        renderCoroutinesDispatcher: CoroutineDispatcher,
    ): Boolean {
        return withContext(renderCoroutinesDispatcher) {
            return@withContext surface
                ?.let {
                    PdfiumCore.surfaceMutex.withLock {
                        document.renderPages(
                            surface,
                            pages.map { page -> page.page },
                            matrices,
                            clipRects,
                            renderAnnot,
                            textMask,
                            canvasColor,
                            pageBackgroundColor,
                        )
                    }
                }
        } ?: false
    }

    /**
     * suspend version of [PdfDocument.deletePage]
     */
    suspend fun deletePage(pageIndex: Int): Unit =
        withContext(dispatcher) {
            document.deletePage(pageIndex)
        }

    /**
     * suspend version of [PdfDocument.getDocumentMeta]
     */
    suspend fun getDocumentMeta(): Either<PdfiumKtFErrors, Meta> =
        wrapEither(dispatcher) {
            document.getDocumentMeta()
        }

    /**
     * suspend version of [PdfDocument.getTableOfContents]
     */
    suspend fun getTableOfContents(): Either<PdfiumKtFErrors, List<Bookmark>> =
        wrapEither(dispatcher) {
            document.getTableOfContents()
        }

    /**
     * suspend version of [io.legere.pdfiumandroid.core.unlocked.PdfDocumentU.openTextPages]
     */
    suspend fun openTextPages(
        fromIndex: Int,
        toIndex: Int,
    ): Either<PdfiumKtFErrors, List<PdfTextPageKtF>> =
        wrapEither(dispatcher) {
            document.openTextPages(fromIndex, toIndex).map { PdfTextPageKtF(it, dispatcher) }
        }

    /**
     * suspend version of [PdfDocument.saveAsCopy]
     */
    suspend fun saveAsCopy(callback: PdfWriteCallback): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
            document.saveAsCopy(callback)
        }

    /**
     * Close the document
     * @throws IllegalArgumentException if document is closed
     */
    override fun close() {
        wrapLock {
            document.close()
        }
    }

    fun safeClose(): Either<PdfiumKtFErrors, Boolean> =
        Either
            .catch {
                wrapLock {
                    document.close()
                }
                true
            }.mapLeft { exceptionToPdfiumKtFError(it) }
}
