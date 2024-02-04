@file:Suppress("unused")

package io.legere.pdfiumandroid

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Surface
import io.legere.pdfiumandroid.util.InitLock
import io.legere.pdfiumandroid.util.Size
import java.io.IOException

/**
 * PdfiumCore is the main entry-point for access to the PDFium API.
 */
@Suppress("TooManyFunctions")
class PdfiumCore(context: Context? = null, logger: LoggerInterface = DefaultLogger()) {

    private val mCurrentDpi: Int

    init {
        Logger.setLogger(logger)
        Logger.d(TAG, "Starting PdfiumAndroid ")
        mCurrentDpi = context?.resources?.displayMetrics?.densityDpi ?: -1
        isReader.waitForReady()
    }

    private external fun nativeOpenDocument(fd: Int, password: String?): Long
    private external fun nativeOpenMemDocument(data: ByteArray?, password: String?): Long

    private external fun nativeGetLinkRect(linkPtr: Long): RectF?

    /**
     * Create new document from file
     * @param fd opened file descriptor of file
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor): PdfDocument {
        return newDocument(fd, null)
    }

    /**
     * Create new document from file with password
     * @param parcelFileDescriptor opened file descriptor of file
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(parcelFileDescriptor: ParcelFileDescriptor, password: String?): PdfDocument {
        synchronized(lock) {
            return PdfDocument(nativeOpenDocument(parcelFileDescriptor.fd, password)).also { document ->
                document.parcelFileDescriptor = parcelFileDescriptor
            }
        }
    }

    /**
     * Create new document from bytearray
     * @param data bytearray of pdf file
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(data: ByteArray?): PdfDocument {
        return newDocument(data, null)
    }

    /**
     * Create new document from bytearray with password
     * @param data bytearray of pdf file
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(data: ByteArray?, password: String?): PdfDocument {
        synchronized(lock) {
            return PdfDocument(nativeOpenMemDocument(data, password)).also { document ->
                document.parcelFileDescriptor = null
            }
        }
    }

    @Deprecated("Use PdfDocument.getPageCount()", ReplaceWith("pdfDocument.getPageCount()"), DeprecationLevel.WARNING)
    fun getPageCount(pdfDocument: PdfDocument) {
        pdfDocument.getPageCount()
    }

    @Deprecated("Use PdfDocument.closeDocument()", ReplaceWith("pdfDocument.close()"), DeprecationLevel.WARNING)
    fun closeDocument(pdfDocument: PdfDocument) {
        pdfDocument.close()
    }

    @Deprecated(
        "Use PdfDocument.getTableOfContents()",
        ReplaceWith("pdfDocument.getTableOfContents()"),
        DeprecationLevel.WARNING
    )
    fun getTableOfContents(pdfDocument: PdfDocument): List<PdfDocument.Bookmark> {
        return pdfDocument.getTableOfContents()
    }

    @Suppress("UNUSED_PARAMETER") // Need to keep for compatibility
    @Deprecated(
        "Use PdfDocument.openTextPage()",
        ReplaceWith("pdfDocument.openTextPage(pageIndex)"),
        DeprecationLevel.WARNING
    )
    fun openTextPage(pdfDocument: PdfDocument, pageIndex: Int): Long {
        return pageIndex.toLong()
    }

    @Suppress("UNUSED_PARAMETER") // Need to keep for compatibility
    @Deprecated(
        "Use PdfDocument.openPage()",
        ReplaceWith("pdfDocument.openPage(pageIndex)"),
        DeprecationLevel.WARNING
    )
    fun openPage(pdfDocument: PdfDocument, pageIndex: Int): Long {
        return pageIndex.toLong()
    }

    @Deprecated(
        "Use Page.getPageMediaBox()",
        ReplaceWith("page.getPageMediaBox()"),
        DeprecationLevel.WARNING
    )
    fun getPageMediaBox(pdfDocument: PdfDocument, pageIndex: Int): RectF {
        pdfDocument.openPage(pageIndex).use { page ->
            return page.getPageMediaBox()
        }
    }

    @Suppress("EmptyMethod")
    @Deprecated(
        "Use page.close()",
        ReplaceWith("page.close()"),
        DeprecationLevel.ERROR
    )
    fun closePage(pdfDocument: PdfDocument, pageIndex: Int) {
        // empty
    }

    @Suppress("UNUSED_PARAMETER", "EmptyMethod") // Need to keep for compatibility
    @Deprecated(
        "Use textPage.close()",
        ReplaceWith("textPage.close()"),
        DeprecationLevel.ERROR
    )
    fun closeTextPage(pdfDocument: PdfDocument, pageIndex: Int) {
        // empty
    }

    @Deprecated(
        "Use textPage.textPageCountChars()",
        ReplaceWith("textPage.textPageCountChars()"),
        DeprecationLevel.WARNING
    )
    fun textPageCountChars(pdfDocument: PdfDocument, pageIndex: Int): Int {
        pdfDocument.openPage(pageIndex).use { page ->
            page.openTextPage().use { textPage ->
                return textPage.textPageCountChars()
            }
        }
    }

    @Deprecated(
        "Use textPage.textPageGetText(start, count)",
        ReplaceWith("textPage.textPageGetText(start, count)"),
        DeprecationLevel.WARNING
    )
    fun textPageGetText(pdfDocument: PdfDocument, pageIndex: Int, start: Int, count: Int): String? {
        pdfDocument.openPage(pageIndex).use { page ->
            page.openTextPage().use { textPage ->
                return textPage.textPageGetText(start, count)
            }
        }
    }

    @Deprecated(
        "Use pdfDocument.getDocumentMeta()",
        ReplaceWith("pdfDocument.getDocumentMeta()"),
        DeprecationLevel.WARNING
    )
    fun getDocumentMeta(pdfDocument: PdfDocument): PdfDocument.Meta {
        return pdfDocument.getDocumentMeta()
    }

    @Deprecated(
        "Use PdfPage.getPageWidthPoint()",
        ReplaceWith("page.getPageWidthPoint()"),
        DeprecationLevel.WARNING
    )
    fun getPageWidthPoint(pdfDocument: PdfDocument, pageIndex: Int): Int {
        pdfDocument.openPage(pageIndex).use { page ->
            return page.getPageWidthPoint()
        }
    }

    @Deprecated(
        "Use PdfPage.getPageHeightPoint()",
        ReplaceWith("page.getPageHeightPoint()"),
        DeprecationLevel.WARNING
    )
    fun getPageHeightPoint(pdfDocument: PdfDocument, pageIndex: Int): Int {
        pdfDocument.openPage(pageIndex).use { page ->
            return page.getPageHeightPoint()
        }
    }

    @Deprecated(
        "Use PdfPage.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, screenDpi, renderAnnot, textMask)",
        ReplaceWith(
            "page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, screenDpi, renderAnnot, textMask)"
        ),
        DeprecationLevel.WARNING
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
        textMask: Boolean = false
    ) {
        pdfDocument.openPage(pageIndex).use { page ->
            page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot, textMask)
        }
    }

    @Deprecated(
        "Use PdfPage.textPageGetRect(index)",
        ReplaceWith(
            "page.textPageGetRect(index)"
        ),
        DeprecationLevel.WARNING
    )
    fun textPageGetRect(pdfDocument: PdfDocument, pageIndex: Int, index: Int): RectF? {
        pdfDocument.openPage(pageIndex).use { page ->
            page.openTextPage().use { textPage ->
                return textPage.textPageGetRect(index)
            }
        }
    }

    @Deprecated(
        "Use PdfPage.textPageGetBoundedText(sourceRect, size)",
        ReplaceWith(
            "page.textPageGetBoundedText(sourceRect, size)"
        ),
        DeprecationLevel.WARNING
    )
    fun textPageGetBoundedText(pdfDocument: PdfDocument, pageIndex: Int, sourceRect: RectF, size: Int): String? {
        pdfDocument.openPage(pageIndex).use { page ->
            page.openTextPage().use { textPage ->
                return textPage.textPageGetBoundedText(sourceRect, size)
            }
        }
    }

    @Deprecated(
        "Use PdfPage.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)",
        ReplaceWith(
            "page.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)"
        ),
        DeprecationLevel.WARNING
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
        coords: Rect
    ): RectF {
        pdfDocument.openPage(pageIndex).use { page ->
            return page.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)
        }
    }

    @Deprecated(
        "Use PdfTextPage.textPageCountRects(startIndex, count)",
        ReplaceWith(
            "textPage.textPageCountRects(startIndex, count)"
        ),
        DeprecationLevel.WARNING
    )
    fun textPageCountRects(
        pdfDocument: PdfDocument,
        pageIndex: Int,
        startIndex: Int,
        count: Int
    ): Int {
        pdfDocument.openPage(pageIndex).use { page ->
            page.openTextPage().use { textPage ->
                return textPage.textPageCountRects(startIndex, count)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER") // Need to keep for compatibility
    @Deprecated(
        "Use PdfDocument.openPage(fromIndex, toIndex)",
        ReplaceWith(
            "pdfDocument.openPage(fromIndex, toIndex)"
        ),
        DeprecationLevel.ERROR
    )
    fun openPage(pdfDocument: PdfDocument, fromIndex: Int, toIndex: Int): Array<Long> {
        return (fromIndex.toLong()..toIndex.toLong()).toList().toTypedArray()
    }

    @Deprecated(
        "Use PdfPage.getPageWidth()",
        ReplaceWith(
            "page.getPageWidth()"
        ),
        DeprecationLevel.WARNING
    )
    fun getPageWidth(pdfDocument: PdfDocument, index: Int): Int {
        pdfDocument.openPage(index).use { page ->
            return page.getPageWidth(mCurrentDpi)
        }
    }

    @Deprecated(
        "Use PdfPage.getPageHeight()",
        ReplaceWith(
            "page.getPageHeight()"
        ),
        DeprecationLevel.WARNING
    )
    fun getPageHeight(pdfDocument: PdfDocument, index: Int): Int {
        pdfDocument.openPage(index).use { page ->
            return page.getPageHeight(mCurrentDpi)
        }
    }

    @Deprecated(
        "Use PdfPage.getPageSize()",
        ReplaceWith(
            "page.getPageSize()"
        ),
        DeprecationLevel.WARNING
    )
    fun getPageSize(pdfDocument: PdfDocument, index: Int): Size {
        pdfDocument.openPage(index).use { page ->
            return page.getPageSize(mCurrentDpi)
        }
    }

    @Deprecated(
        "Use PdfPage.renderPage(surface, startX, startY, drawSizeX, drawSizeY)",
        ReplaceWith(
            "page.renderPage(surface, startX, startY, drawSizeX, drawSizeY)"
        ),
        DeprecationLevel.WARNING
    )
    @Suppress("LongParameterList")
    fun renderPage(
        pdfDocument: PdfDocument,
        surface: Surface?,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false
    ) {
        pdfDocument.openPage(pageIndex).use { page ->
            page.renderPage(surface, startX, startY, drawSizeX, drawSizeY, false)
        }
    }

    @Deprecated(
        "Use PdfPage.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY)",
        ReplaceWith(
            "page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY)"
        ),
        DeprecationLevel.WARNING
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
        renderAnnot: Boolean = false
    ) {
        pdfDocument.openPage(pageIndex).use { page ->
            page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot)
        }
    }

    @Deprecated(
        "Use PdfPage.getPageLinks()",
        ReplaceWith(
            "page.getPageLinks()"
        ),
        DeprecationLevel.WARNING
    )
    @Suppress("LongParameterList")
    fun getPageLinks(pdfDocument: PdfDocument, pageIndex: Int): List<PdfDocument.Link> {
        pdfDocument.openPage(pageIndex).use { page ->
            return page.getPageLinks()
        }
    }

    @Deprecated(
        "Use PdfPage.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)",
        ReplaceWith(
            "page.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)"
        ),
        DeprecationLevel.WARNING
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
        pageY: Double
    ): Point {
        pdfDocument.openPage(pageIndex).use { page ->
            return page.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)
        }
    }

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
        coords: RectF
    ): Rect {
        pdfDocument.openPage(pageIndex).use { page ->
            return page.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords)
        }
    }

    companion object {
        private val TAG = PdfiumCore::class.java.name

        /* synchronize native methods */
        val lock = Any()

        val isReader = InitLock()

        init {
            Log.d(TAG, "init")
            Thread {
                Log.d(TAG, "init thread start")
                synchronized(lock) {
                    Log.d(TAG, "init in lock")
                    try {
                        System.loadLibrary("absl.cr")
                        System.loadLibrary("c++_chrome.cr")
                        System.loadLibrary("chrome_zlib.cr")
                        System.loadLibrary("icuuc.cr")
                        System.loadLibrary("partition_alloc.cr")
                        System.loadLibrary("pdfium.cr")
                        System.loadLibrary("pdfiumandroid")
                        isReader.markReady()
                    } catch (e: UnsatisfiedLinkError) {
                        Logger.e(TAG, e, "Native libraries failed to load")
                    }
                    Log.d(TAG, "init in lock")
                }
            }.start()
        }
    }
}
