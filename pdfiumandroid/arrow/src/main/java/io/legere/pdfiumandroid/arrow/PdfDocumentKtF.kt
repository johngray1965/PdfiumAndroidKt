@file:Suppress("unused", "CanBeVal")

package io.legere.pdfiumandroid.arrow

import android.graphics.Matrix
import android.graphics.RectF
import android.view.Surface
import arrow.core.Either
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfPage
import io.legere.pdfiumandroid.PdfWriteCallback
import io.legere.pdfiumandroid.PdfiumCore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
class PdfDocumentKtF(
    val document: PdfDocument,
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
            PdfPageKtF(document.openPage(pageIndex), dispatcher)
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
        matrices: List<Matrix>,
        clipRects: List<RectF>,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ) {
        PdfiumCore.surfaceMutex.withLock {
            val sizes = IntArray(2)
            val pointers = LongArray(2)
            withContext(Dispatchers.Main) {
                surface
                    ?.let {
                        PdfPage.lockSurface(
                            it,
                            sizes,
                            pointers,
                        )
                    }
            }
            val nativeWindow = pointers[0]
            val bufferPtr = pointers[1]
            val surfaceWidth = sizes[0]
            val surfaceHeight = sizes[1]
            if (bufferPtr == 0L || bufferPtr == -1L || nativeWindow == 0L || nativeWindow == -1L) {
                return
            }
            withContext(dispatcher) {
                document.renderPages(
                    bufferPtr,
                    surfaceWidth,
                    surfaceHeight,
                    pages.map { page -> page.page },
                    matrices,
                    clipRects,
                    renderAnnot,
                    textMask,
                    canvasColor,
                    pageBackgroundColor,
                )
            }
            withContext(Dispatchers.Main) {
                surface?.let {
                    Logger.d(
                        "PdfDocumentKtF",
                        "releasing " +
                            "pages: ${pages.map { it.page.pageIndex }.joinToString()}, " +
                            " nativeWindow: $nativeWindow, " +
                            "bufferPtr: $bufferPtr",
                    )
                    PdfPage.unlockSurface(longArrayOf(nativeWindow, bufferPtr))
                }
            }
        }
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
    suspend fun getDocumentMeta(): Either<PdfiumKtFErrors, PdfDocument.Meta> =
        wrapEither(dispatcher) {
            document.getDocumentMeta()
        }

    /**
     * suspend version of [PdfDocument.getTableOfContents]
     */
    suspend fun getTableOfContents(): Either<PdfiumKtFErrors, List<PdfDocument.Bookmark>> =
        wrapEither(dispatcher) {
            document.getTableOfContents()
        }

    /**
     * suspend version of [PdfDocument.openTextPages]
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
        document.close()
    }

    fun safeClose(): Either<PdfiumKtFErrors, Boolean> =
        Either
            .catch {
                document.close()
                true
            }.mapLeft { exceptionToPdfiumKtFError(it) }
}
