@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.graphics.Matrix
import android.graphics.RectF
import android.view.Surface
import androidx.annotation.Keep
import io.legere.pdfiumandroid.LockManager
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfWriteCallback
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.util.pdfiumConfig
import kotlinx.coroutines.CoroutineDispatcher
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
class PdfDocumentKt(
    internal val document: PdfDocumentU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    private val lock: LockManager = pdfiumConfig.lock

    fun getDocument(): PdfDocument = PdfDocument(document)

    /**
     *  suspend version of [PdfDocument.getPageCount]
     */
    suspend fun getPageCount(): Int =
        lock.withLock {
            withContext(dispatcher) {
                document.getPageCount()
            }
        }

    /**
     *  suspend version of [PdfDocument.getPageCharCounts]
     */
    suspend fun getPageCharCounts(): IntArray =
        lock.withLock {
            withContext(dispatcher) {
                document.getPageCharCounts()
            }
        }

    /**
     * suspend version of [PdfDocument.openPage]
     */
    suspend fun openPage(pageIndex: Int): PdfPageKt? =
        lock.withLock {
            withContext(dispatcher) {
                document.openPage(pageIndex)?.let { PdfPageKt(it, dispatcher) }
            }
        }

    /**
     * suspend version of [PdfDocument.deletePage]
     */
    suspend fun deletePage(pageIndex: Int): Unit =
        lock.withLock {
            withContext(dispatcher) {
                document.deletePage(pageIndex)
            }
        }

    /**
     * suspend version of [PdfDocument.openPages]
     */
    suspend fun openPages(
        fromIndex: Int,
        toIndex: Int,
    ): List<PdfPageKt> =
        lock.withLock {
            withContext(dispatcher) {
                document.openPages(fromIndex, toIndex).map { PdfPageKt(it, dispatcher) }
            }
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
    ): Boolean {
        PdfiumCore.surfaceMutex.withLock {
            return withContext(renderCoroutinesDispatcher) {
                return@withContext document.renderPages(
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
    }

    /**
     * suspend version of [PdfDocument.getDocumentMeta]
     */
    suspend fun getDocumentMeta(): PdfDocument.Meta =
        lock.withLock {
            withContext(dispatcher) {
                document.getDocumentMeta()
            }
        }

    /**
     * suspend version of [PdfDocument.getTableOfContents]
     */
    suspend fun getTableOfContents(): List<PdfDocument.Bookmark> =
        lock.withLock {
            withContext(dispatcher) {
                document.getTableOfContents()
            }
        }

    /**
     * suspend version of [PdfDocument.openTextPage]
     */
    @Deprecated("use PdfPageKt.openTextPage", ReplaceWith("page.openTextPage()"))
    @Suppress("DEPRECATION")
    suspend fun openTextPage(page: PdfPageKt): PdfTextPageKt =
        lock.withLock {
            withContext(dispatcher) {
                PdfTextPageKt(document.openTextPage(page.page), dispatcher)
            }
        }

    /**
     * suspend version of [PdfDocument.openTextPages]
     */
    suspend fun openTextPages(
        fromIndex: Int,
        toIndex: Int,
    ): List<PdfTextPageKt> =
        lock.withLock {
            withContext(dispatcher) {
                document.openTextPages(fromIndex, toIndex).map { PdfTextPageKt(it, dispatcher) }
            }
        }

    /**
     * suspend version of [PdfDocument.saveAsCopy]
     */
    suspend fun saveAsCopy(callback: PdfWriteCallback): Boolean =
        lock.withLock {
            withContext(dispatcher) {
                document.saveAsCopy(callback)
            }
        }

    /**
     * Close the document
     * @throws IllegalArgumentException if document is closed
     */
    override fun close() {
        lock.withLockBlocking {
            document.close()
        }
    }

    fun safeClose(): Boolean =
        try {
            lock.withLockBlocking {
                document.close()
            }
            true
        } catch (e: IllegalStateException) {
            Logger.e("PdfDocumentKt", e, "PdfDocumentKt.safeClose")
            false
        }
}
