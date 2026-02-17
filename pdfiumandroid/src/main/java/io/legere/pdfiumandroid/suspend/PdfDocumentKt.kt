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

package io.legere.pdfiumandroid.suspend

import android.graphics.Matrix
import android.graphics.RectF
import android.view.Surface
import androidx.annotation.Keep
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.api.Bookmark
import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.api.Meta
import io.legere.pdfiumandroid.api.PdfWriteCallback
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.util.wrapLock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * PdfDocumentKt represents a PDF file and allows you to load pages from it.
 * @property document the [PdfDocument] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 * @constructor create a [PdfDocumentKt] from a [PdfDocument]
 */
@Suppress("TooManyFunctions")
@Keep
class PdfDocumentKt internal constructor(
    internal val document: PdfDocumentU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    fun getDocument(): PdfDocument = PdfDocument(document)

    /**
     *  suspend version of [PdfDocument.getPageCount]
     */
    suspend fun getPageCount(): Int =
        wrapSuspend(dispatcher) {
            document.getPageCount()
        }

    /**
     *  suspend version of [PdfDocument.getPageCharCounts]
     */
    suspend fun getPageCharCounts(): IntArray =
        wrapSuspend(dispatcher) {
            document.getPageCharCounts()
        }

    /**
     * suspend version of [PdfDocument.openPage]
     */
    suspend fun openPage(pageIndex: Int): PdfPageKt? =
        wrapSuspend(dispatcher) {
            document.openPage(pageIndex)?.let { PdfPageKt(it, dispatcher) }
        }

    /**
     * suspend version of [PdfDocument.deletePage]
     */
    suspend fun deletePage(pageIndex: Int): Unit =
        wrapSuspend(dispatcher) {
            document.deletePage(pageIndex)
        }

    /**
     * suspend version of [PdfDocument.openPages]
     */
    suspend fun openPages(
        fromIndex: Int,
        toIndex: Int,
    ): List<PdfPageKt> =
        wrapSuspend(dispatcher) {
            document.openPages(fromIndex, toIndex).map { PdfPageKt(it, dispatcher) }
        }

    /**
     * suspend version of [PdfDocument.renderPages]
     */
    @Suppress("LongParameterList", "ComplexMethod", "ComplexCondition")
    suspend fun renderPages(
        surface: Surface,
        pages: List<PdfPageKt>,
        matrices: List<Matrix>,
        clipRects: List<RectF>,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
        renderCoroutinesDispatcher: CoroutineDispatcher,
    ): Boolean =
        withContext(renderCoroutinesDispatcher) {
            PdfiumCore.surfaceMutex.withLock {
                if (!coroutineContext.isActive) return@withContext false
                document.renderPages(
                    surface,
                    pages.map { it.page },
                    matrices,
                    clipRects,
                    renderAnnot,
                    textMask,
                    canvasColor,
                    pageBackgroundColor,
                )
            }
        }

    /**
     * suspend version of [PdfDocument.getDocumentMeta]
     */
    suspend fun getDocumentMeta(): Meta =
        wrapSuspend(dispatcher) {
            document.getDocumentMeta()
        }

    /**
     * suspend version of [PdfDocument.getTableOfContents]
     */
    suspend fun getTableOfContents(): List<Bookmark> =
        wrapSuspend(dispatcher) {
            document.getTableOfContents()
        }

    /**
     * suspend version of [PdfDocument.openTextPage]
     * @deprecated
     */
    @Deprecated("use PdfPageKt.openTextPage", ReplaceWith("page.openTextPage()"))
    @Suppress("DEPRECATION")
    suspend fun openTextPage(page: PdfPageKt): PdfTextPageKt =
        wrapSuspend(dispatcher) {
            PdfTextPageKt(document.openTextPage(page.page), dispatcher)
        }

    /**
     * suspend version of [PdfDocument.openTextPages]
     */
    suspend fun openTextPages(
        fromIndex: Int,
        toIndex: Int,
    ): List<PdfTextPageKt> =
        wrapSuspend(dispatcher) {
            document.openTextPages(fromIndex, toIndex).map { PdfTextPageKt(it, dispatcher) }
        }

    /**
     * suspend version of [PdfDocument.saveAsCopy]
     */
    suspend fun saveAsCopy(callback: PdfWriteCallback): Boolean =
        wrapSuspend(dispatcher) {
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

    fun safeClose(): Boolean =
        try {
            wrapLock {
                document.close()
            }
            true
        } catch (e: IllegalStateException) {
            Logger.e("PdfDocumentKt", e, "PdfDocumentKt.safeClose")
            false
        }
}
