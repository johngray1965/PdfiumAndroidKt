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

package io.legere.pdfiumandroid

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import io.legere.pdfiumandroid.api.Bookmark
import io.legere.pdfiumandroid.api.Config
import io.legere.pdfiumandroid.api.Link
import io.legere.pdfiumandroid.api.LockManager
import io.legere.pdfiumandroid.api.Meta
import io.legere.pdfiumandroid.api.PdfiumSource
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU.Companion.lock
import io.legere.pdfiumandroid.core.util.wrapLock
import kotlinx.coroutines.sync.Mutex
import java.io.IOException

/**
 * `PdfiumCore` is the main entry-point for accessing the PDFium API in a thread-safe manner.
 * It manages the lifecycle of PDF documents and provides high-level operations for
 * creating, accessing, and rendering PDF files.
 *
 * This class handles thread synchronization internally using a global lock, ensuring
 * that native PDFium calls are executed safely without race conditions.
 * For raw access to the native API, refer to [io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU]
 * (for internal library use only).
 *
 * @param context An Android [Context] for retrieving display metrics and other resources.
 * @param config A [io.legere.pdfiumandroid.api.Config] object to customize library behavior, such
 * as logging and error handling.
 * @param coreInternal An internal [io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU] instance for
 * raw native access. Defaults to a new instance.
 */
@Suppress("TooManyFunctions")
class PdfiumCore(
    context: Context? = null,
    val config: Config = Config(),
    private val coreInternal: PdfiumCoreU = PdfiumCoreU(config = config),
) {
    /**
     * Creates a new [PdfDocument] from a [ParcelFileDescriptor].
     * The document is opened without a password.
     *
     * @param parcelFileDescriptor The opened file descriptor of the PDF file.
     * @return A [PdfDocument] instance representing the opened PDF file.
     * @throws IOException if the PDF document cannot be opened (e.g., file not found,
     * corrupted, or password protected).
     */
    @Throws(IOException::class)
    fun newDocument(parcelFileDescriptor: ParcelFileDescriptor): PdfDocument = newDocument(parcelFileDescriptor, null)

    /**
     * Creates a new [PdfDocument] from a [ParcelFileDescriptor] with a password.
     *
     * @param parcelFileDescriptor The opened file descriptor of the PDF file.
     * @param password The password for decrypting the PDF document, or `null` if no password is required.
     * @return A [PdfDocument] instance representing the opened PDF file.
     * @throws IOException if the PDF document cannot be opened (e.g., file not found,
     * corrupted, or incorrect password).
     */
    @Throws(IOException::class)
    fun newDocument(
        parcelFileDescriptor: ParcelFileDescriptor,
        password: String?,
    ): PdfDocument =
        wrapLock {
            PdfDocument(coreInternal.newDocument(parcelFileDescriptor, password))
        }

    /**
     * Creates a new [PdfDocument] from a byte array.
     * The document is opened without a password.
     *
     * @param data The byte array containing the PDF file content.
     * @return A [PdfDocument] instance representing the opened PDF file.
     * @throws IOException if the PDF document cannot be opened (e.g., corrupted or password protected).
     */
    @Throws(IOException::class)
    fun newDocument(data: ByteArray?): PdfDocument = newDocument(data, null)

    /**
     * Creates a new [PdfDocument] from a byte array with a password.
     *
     * @param data The byte array containing the PDF file content.
     * @param password The password for decrypting the PDF document, or `null` if no password is required.
     * @return A [PdfDocument] instance representing the opened PDF file.
     * @throws IOException if the PDF document cannot be opened (e.g., corrupted or incorrect password).
     */
    @Throws(IOException::class)
    fun newDocument(
        data: ByteArray?,
        password: String?,
    ): PdfDocument =
        wrapLock {
            PdfDocument(coreInternal.newDocument(data, password))
        }

    /**
     * Creates a new [PdfDocument] from a custom [io.legere.pdfiumandroid.api.PdfiumSource].
     * The document is opened without a password.
     *
     * @param data The custom data source to read the PDF file content from.
     * @return A [PdfDocument] instance representing the opened PDF file.
     * @throws IOException if the PDF document cannot be opened.
     */
    @Throws(IOException::class)
    fun newDocument(data: PdfiumSource): PdfDocument = newDocument(data, null)

    /**
     * Creates a new [PdfDocument] from a custom [PdfiumSource] with a password.
     *
     * @param data The custom data source to read the PDF file content from.
     * @param password The password for decrypting the PDF document, or `null` if no password is required.
     * @return A [PdfDocument] instance representing the opened PDF file.
     * @throws IOException if the PDF document cannot be opened.
     */
    @Throws(IOException::class)
    fun newDocument(
        data: PdfiumSource,
        password: String?,
    ): PdfDocument =
        wrapLock {
            PdfDocument(coreInternal.newDocument(data, password))
        }

    /**
     * @deprecated Use [PdfDocument.getPageCount] instead.
     */
    @Deprecated("Use PdfDocument.getPageCount()", ReplaceWith("pdfDocument.getPageCount()"), DeprecationLevel.WARNING)
    fun getPageCount(pdfDocument: PdfDocument): Int = pdfDocument.getPageCount()

    /**
     * @deprecated Use [PdfDocument.close] instead.
     */
    @Deprecated("Use PdfDocument.closeDocument()", ReplaceWith("pdfDocument.close()"), DeprecationLevel.WARNING)
    fun closeDocument(pdfDocument: PdfDocument) {
        pdfDocument.close()
    }

    /**
     * @deprecated Use [PdfDocument.getTableOfContents] instead.
     */
    @Deprecated(
        "Use PdfDocument.getTableOfContents()",
        ReplaceWith("pdfDocument.getTableOfContents()"),
        DeprecationLevel.WARNING,
    )
    fun getTableOfContents(pdfDocument: PdfDocument): List<Bookmark> = pdfDocument.getTableOfContents()

    /**
     * @deprecated Use [PdfPage.openTextPage] after obtaining a [PdfPage] from [PdfDocument.openPage].
     */
    @Suppress("UNUSED_PARAMETER") // Need to keep for compatibility
    @Deprecated(
        "Use PdfDocument.openTextPage()",
        ReplaceWith("pdfDocument.openTextPage(pageIndex)"),
        DeprecationLevel.WARNING,
    )
    fun openTextPage(pdfDocument: PdfDocument, pageIndex: Int): Long = pageIndex.toLong()

    /**
     * @deprecated Use [PdfDocument.openPage] instead.
     */
    @Suppress("UNUSED_PARAMETER") // Need to keep for compatibility
    @Deprecated(
        "Use PdfDocument.openPage()",
        ReplaceWith("pdfDocument.openPage(pageIndex)"),
        DeprecationLevel.WARNING,
    )
    fun openPage(pdfDocument: PdfDocument, pageIndex: Int): Long = pageIndex.toLong()

    /**
     * @deprecated Use [PdfPage.getPageMediaBox] after obtaining a [PdfPage] from [PdfDocument.openPage].
     */
    @Deprecated(
        "Use Page.getPageMediaBox()",
        ReplaceWith("page.getPageMediaBox()"),
        DeprecationLevel.WARNING,
    )
    fun getPageMediaBox(
        pdfDocument: PdfDocument,
        pageIndex: Int,
    ): RectF {
        pdfDocument.openPage(pageIndex).use { page ->
            return page?.getPageMediaBox() ?: RectF(-1f, -1f, -1f, -1f)
        }
    }

    /**
     * @deprecated Use [PdfPage.close] instead.
     */
    @Suppress("EmptyMethod")
    @Deprecated(
        "Use page.close()",
        ReplaceWith("page.close()"),
        DeprecationLevel.ERROR,
    )
    fun closePage(
        pdfDocument: PdfDocument,
        pageIndex: Int,
    ) {
        // empty
    }

    /**
     * @deprecated Use [PdfTextPage.close] after obtaining a [PdfTextPage].
     */
    @Suppress("UNUSED_PARAMETER", "EmptyMethod") // Need to keep for compatibility
    @Deprecated(
        "Use textPage.close()",
        ReplaceWith("textPage.close()"),
        DeprecationLevel.ERROR,
    )
    fun closeTextPage(pdfDocument: PdfDocument, pageIndex: Int) {
        // empty
    }

    /**
     * @deprecated Use [PdfTextPage.textPageCountChars] after obtaining a [PdfTextPage] from [PdfPage.openTextPage].
     */
    @Deprecated(
        "Use textPage.textPageCountChars()",
        ReplaceWith("textPage.textPageCountChars()"),
        DeprecationLevel.WARNING,
    )
    fun textPageCountChars(
        pdfDocument: PdfDocument,
        pageIndex: Int,
    ): Int {
        pdfDocument.openPage(pageIndex).use { page ->
            val ret =
                page?.openTextPage()?.use { textPage ->
                    return textPage.textPageCountChars()
                }
            return ret ?: -1
        }
    }

    /**
     * @deprecated Use [PdfTextPage.textPageGetText] after obtaining a [PdfTextPage] from [PdfPage.openTextPage].
     */
    @Deprecated(
        "Use textPage.textPageGetText(start, count)",
        ReplaceWith("textPage.textPageGetText(start, count)"),
        DeprecationLevel.WARNING,
    )
    fun textPageGetText(
        pdfDocument: PdfDocument,
        pageIndex: Int,
        start: Int,
        count: Int,
    ): String? {
        pdfDocument.openPage(pageIndex).use { page ->
            return page?.openTextPage()?.use { textPage ->
                textPage.textPageGetText(start, count)
            }
        }
    }

    /**
     * @deprecated Use [PdfDocument.getDocumentMeta] instead.
     */
    @Deprecated(
        "Use pdfDocument.getDocumentMeta()",
        ReplaceWith("pdfDocument.getDocumentMeta()"),
        DeprecationLevel.WARNING,
    )
    fun getDocumentMeta(pdfDocument: PdfDocument): Meta = pdfDocument.getDocumentMeta()

    /**
     * @deprecated Use [PdfPage.getPageWidthPoint] after obtaining a [PdfPage] from [PdfDocument.openPage].
     */
    @Deprecated(
        "Use PdfPage.getPageWidthPoint()",
        ReplaceWith("page.getPageWidthPoint()"),
        DeprecationLevel.WARNING,
    )
    fun getPageWidthPoint(
        pdfDocument: PdfDocument,
        pageIndex: Int,
    ): Int {
        pdfDocument.openPage(pageIndex).use { page ->
            return page?.getPageWidthPoint() ?: -1
        }
    }

    /**
     * @deprecated Use [PdfPage.getPageHeightPoint] after obtaining a [PdfPage] from [PdfDocument.openPage].
     */
    @Deprecated(
        "Use PdfPage.getPageHeightPoint()",
        ReplaceWith("page.getPageHeightPoint()"),
        DeprecationLevel.WARNING,
    )
    fun getPageHeightPoint(
        pdfDocument: PdfDocument,
        pageIndex: Int,
    ): Int {
        pdfDocument.openPage(pageIndex).use { page ->
            return page?.getPageHeightPoint() ?: -1
        }
    }

    /**
     * @deprecated Use [PdfPage.renderPageBitmap] after obtaining a [PdfPage] from [PdfDocument.openPage].
     * This overload does not include `screenDpi` which is now handled by the `PdfPage` itself or assumed via matrix.
     */
    @Deprecated(
        "Use PdfPage.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, screenDpi, renderAnnot, textMask)",
        ReplaceWith(
            "page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, screenDpi, renderAnnot, textMask)",
        ),
        DeprecationLevel.WARNING,
    )
    @Suppress("LongParameterList")
    fun renderPageBitmap(
        pdfDocument: PdfDocument,
        bitmap: Bitmap?,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
    ) {
        pdfDocument.openPage(pageIndex).use { page ->
            page?.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot, textMask)
        }
    }

    /**
     * @deprecated Use [PdfTextPage.textPageGetRect] after obtaining a [PdfTextPage] from [PdfPage.openTextPage].
     */
    @Deprecated(
        "Use PdfPage.textPageGetRect(index)",
        ReplaceWith(
            "page.textPageGetRect(index)",
        ),
        DeprecationLevel.WARNING,
    )
    fun textPageGetRect(
        pdfDocument: PdfDocument,
        pageIndex: Int,
        index: Int,
    ): RectF? {
        pdfDocument.openPage(pageIndex).use { page ->
            return page?.openTextPage()?.use { textPage ->
                textPage.textPageGetRect(index)
            }
        }
    }

    /**
     * @deprecated Use [PdfTextPage.textPageGetBoundedText] after obtaining a [PdfTextPage] from [PdfPage.openTextPage].
     */
    @Deprecated(
        "Use PdfPage.textPageGetBoundedText(sourceRect, size)",
        ReplaceWith(
            "page.textPageGetBoundedText(sourceRect, size)",
        ),
        DeprecationLevel.WARNING,
    )
    fun textPageGetBoundedText(
        pdfDocument: PdfDocument,
        pageIndex: Int,
        sourceRect: RectF,
        size: Int,
    ): String? {
        pdfDocument.openPage(pageIndex).use { page ->
            return page?.openTextPage()?.use { textPage ->
                textPage.textPageGetBoundedText(sourceRect, size)
            }
        }
    }

    /**
     * @deprecated Use [PdfPage.mapRectToPage] after obtaining a [PdfPage] from [PdfDocument.openPage].
     */
    @Deprecated(
        "Use PdfPage.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)",
        ReplaceWith(
            "page.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)",
        ),
        DeprecationLevel.WARNING,
    )
    @Suppress("LongParameterList")
    fun mapRectToPage(
        pdfDocument: PdfDocument,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: Rect,
    ): RectF {
        pdfDocument.openPage(pageIndex).use { page ->
            return page?.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords) ?: RectF(-1f, -1f, -1f, -1f)
        }
    }

    /**
     * @deprecated Use [PdfTextPage.textPageCountRects] after obtaining a [PdfTextPage] from [PdfPage.openTextPage].
     */
    @Deprecated(
        "Use PdfTextPage.textPageCountRects(startIndex, count)",
        ReplaceWith(
            "textPage.textPageCountRects(startIndex, count)",
        ),
        DeprecationLevel.WARNING,
    )
    fun textPageCountRects(
        pdfDocument: PdfDocument,
        pageIndex: Int,
        startIndex: Int,
        count: Int,
    ): Int {
        pdfDocument.openPage(pageIndex).use { page ->
            page?.openTextPage().use { textPage ->
                return textPage?.textPageCountRects(startIndex, count) ?: -1
            }
        }
    }

    /**
     * @deprecated This method is no longer supported. Use [PdfDocument.openPages] instead.
     */
    @Suppress("UNUSED_PARAMETER") // Need to keep for compatibility
    @Deprecated(
        "Use PdfDocument.openPage(fromIndex, toIndex)",
        ReplaceWith(
            "pdfDocument.openPage(fromIndex, toIndex)",
        ),
        DeprecationLevel.ERROR,
    )
    fun openPage(
        pdfDocument: PdfDocument,
        fromIndex: Int,
        toIndex: Int,
    ): Array<Long> = (fromIndex.toLong()..toIndex.toLong()).toList().toTypedArray()

    /**
     * @deprecated Use [PdfPage.renderPageBitmap] after obtaining a [PdfPage] from [PdfDocument.openPage].
     * This overload does not include `screenDpi` which is now handled by the `PdfPage` itself or assumed via matrix.
     */
    @Deprecated(
        "Use PdfPage.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY)",
        ReplaceWith(
            "page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY)",
        ),
        DeprecationLevel.WARNING,
    )
    @Suppress("LongParameterList")
    fun renderPageBitmap(
        pdfDocument: PdfDocument,
        bitmap: Bitmap?,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false,
    ) {
        pdfDocument.openPage(pageIndex).use { page ->
            page?.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot)
        }
    }

    /**
     * @deprecated Use [PdfPage.getPageLinks] after obtaining a [PdfPage] from [PdfDocument.openPage].
     */
    @Deprecated(
        "Use PdfPage.getPageLinks()",
        ReplaceWith(
            "page.getPageLinks()",
        ),
        DeprecationLevel.WARNING,
    )
    @Suppress("LongParameterList")
    fun getPageLinks(
        pdfDocument: PdfDocument,
        pageIndex: Int,
    ): List<Link> {
        pdfDocument.openPage(pageIndex).use { page ->
            return page?.getPageLinks() ?: emptyList()
        }
    }

    /**
     * @deprecated Use [PdfPage.mapPageCoordsToDevice] after obtaining a [PdfPage] from [PdfDocument.openPage].
     */
    @Deprecated(
        "Use PdfPage.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)",
        ReplaceWith(
            "page.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)",
        ),
        DeprecationLevel.WARNING,
    )
    @Suppress("LongParameterList")
    fun mapPageCoordsToDevice(
        pdfDocument: PdfDocument,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        pageX: Double,
        pageY: Double,
    ): Point {
        pdfDocument.openPage(pageIndex).use { page ->
            return page?.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY) ?: Point(-1, -1)
        }
    }

    /**
     * @deprecated Use [PdfPage.mapRectToDevice] after obtaining a [PdfPage] from [PdfDocument.openPage].
     */
    @Deprecated(
        "Use PdfPage.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords)",
        ReplaceWith(
            "page.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords)",
        ),
        DeprecationLevel.WARNING,
    )
    @Suppress("LongParameterList")
    fun mapRectToDevice(
        pdfDocument: PdfDocument,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: RectF,
    ): Rect {
        pdfDocument.openPage(pageIndex).use { page ->
            return page?.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords) ?: Rect(-1, -1, -1, -1)
        }
    }

    /**
     * Sets the global [io.legere.pdfiumandroid.api.LockManager] for PdfiumAndroidKt.
     * This method allows custom synchronization strategies to be injected into the library.
     *
     * @param lockManager The [io.legere.pdfiumandroid.api.LockManager] implementation to use for
     * thread synchronization.
     */
    fun setLockManager(lockManager: LockManager) {
        lock = lockManager
    }

    /**
     * @suppress
     */
    companion object {
        private val TAG = PdfiumCore::class.java.name

        /**
         * A [Mutex] used for protecting access to the Android [Surface] during rendering operations.
         * This mutex ensures that only one rendering operation to a shared surface can occur at a time.
         */
        val surfaceMutex = Mutex()
    }
}
