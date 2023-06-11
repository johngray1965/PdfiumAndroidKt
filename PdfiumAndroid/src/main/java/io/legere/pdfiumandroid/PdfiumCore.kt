@file:Suppress("unused")

package io.legere.pdfiumandroid

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Surface
import io.legere.pdfiumandroid.util.Size
import java.io.FileDescriptor
import java.io.IOException
import java.lang.reflect.Field

/**
 * PdfiumCore is the main entry-point for access to the PDFium API.
 */
@Suppress("TooManyFunctions")
class PdfiumCore {

    private external fun nativeOpenDocument(fd: Int, password: String?): Long
    private external fun nativeOpenMemDocument(data: ByteArray?, password: String?): Long

    private external fun nativeGetLinkRect(linkPtr: Long): RectF?

    /** Context needed to get screen density  */
    init {
        Log.d(TAG, "Starting PdfiumAndroid ")
    }

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
     * @param fd opened file descriptor of file
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor, password: String?): PdfDocument {
        synchronized(lock) {
            return PdfDocument(nativeOpenDocument(getNumFd(fd), password)).also { document ->
                document.parcelFileDescriptor = fd
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

    @Deprecated("Use PdfDocument.getPageCount()", ReplaceWith("pdfDocument.getPageCount()"), DeprecationLevel.ERROR)
    fun getPageCount(pdfDocument: PdfDocument) {
        error("Method has been moved")
    }

    @Deprecated("Use PdfDocument.closeDocument()", ReplaceWith("pdfDocument.close()"), DeprecationLevel.ERROR)
    fun closeDocument(pdfDocument: PdfDocument) {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfDocument.getTableOfContents()",
        ReplaceWith("pdfDocument.getTableOfContents()"),
        DeprecationLevel.ERROR
    )
    fun getTableOfContents(mPdfDocument: PdfDocument): List<PdfDocument.Bookmark>? {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfDocument.openTextPage()",
        ReplaceWith("pdfDocument.openTextPage(pageIndex)"),
        DeprecationLevel.ERROR
    )
    fun openTextPage(pdfDocument: PdfDocument, pageIndex: Int): Long {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfDocument.openPage()",
        ReplaceWith("pdfDocument.openPage(pageIndex)"),
        DeprecationLevel.ERROR
    )
    fun openPage(pdfDocument: PdfDocument, pageIndex: Int): Long {
        error("Method has been moved")
    }

    @Deprecated(
        "Use Page.getPageMediaBox()",
        ReplaceWith("page.getPageMediaBox()"),
        DeprecationLevel.ERROR
    )
    fun getPageMediaBox(pdfDocument: PdfDocument, pageIndex: Int): RectF {
        error("Method has been moved")
    }

    @Deprecated(
        "Use page.close()",
        ReplaceWith("page.close()"),
        DeprecationLevel.ERROR
    )
    fun closePage(pdfDocument: PdfDocument, pageIndex: Int) {
        error("Method has been moved")
    }

    @Deprecated(
        "Use textPage.close()",
        ReplaceWith("textPage.close()"),
        DeprecationLevel.ERROR
    )
    fun closeTextPage(pdfDocument: PdfDocument, pageIndex: Int) {
        error("Method has been moved")
    }

    @Deprecated(
        "Use textPage.textPageCountChars()",
        ReplaceWith("textPage.textPageCountChars()"),
        DeprecationLevel.ERROR
    )
    fun textPageCountChars(pdfDocument: PdfDocument, pageIndex: Int) {
        error("Method has been moved")
    }

    @Deprecated(
        "Use textPage.textPageGetText(start, count)",
        ReplaceWith("textPage.textPageGetText(start, count)"),
        DeprecationLevel.ERROR
    )
    fun textPageGetText(pdfDocument: PdfDocument, pageIndex: Int, start: Int, count: Int): String {
        error("Method has been moved")
    }

    @Deprecated(
        "Use pdfDocument.getDocumentMeta()",
        ReplaceWith("pdfDocument.getDocumentMeta()"),
        DeprecationLevel.ERROR
    )
    fun getDocumentMeta(pdfDocument: PdfDocument): PdfDocument.Meta? {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.getPageWidthPoint()",
        ReplaceWith("page.getPageWidthPoint()"),
        DeprecationLevel.ERROR
    )
    fun getPageWidthPoint(pdfDocument: PdfDocument, pageIndex: Int): Int {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.getPageHeightPoint()",
        ReplaceWith("page.getPageHeightPoint()"),
        DeprecationLevel.ERROR
    )
    fun getPageHeightPoint(pdfDocument: PdfDocument, pageIndex: Int): Int {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, screenDpi, renderAnnot, textMask)",
        ReplaceWith(
            "page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, screenDpi, renderAnnot, textMask)"
        ),
        DeprecationLevel.ERROR
    )
    @Suppress("LongParameterList")
    fun renderPageBitmap(
        pdfDocument: PdfDocument,
        bitmap: Bitmap?,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        screenDpi: Int,
        renderAnnot: Boolean = false,
        textMask: Boolean = false
    ) {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.textPageGetRect(index)",
        ReplaceWith(
            "page.textPageGetRect(index)"
        ),
        DeprecationLevel.ERROR
    )
    fun textPageGetRect(pdfDocument: PdfDocument, page: Int, index: Int): RectF {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.textPageGetBoundedText(sourceRect, size)",
        ReplaceWith(
            "page.textPageGetBoundedText(sourceRect, size)"
        ),
        DeprecationLevel.ERROR
    )
    fun textPageGetBoundedText(pdfDocument: PdfDocument, pageIndex: Int, sourceRect: RectF, size: Int): RectF {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)",
        ReplaceWith(
            "page.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)"
        ),
        DeprecationLevel.ERROR
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
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfTextPage.textPageCountRects(startIndex, count)",
        ReplaceWith(
            "textPage.textPageCountRects(startIndex, count)"
        ),
        DeprecationLevel.ERROR
    )
    fun textPageCountRects(
        pdfDocument: PdfDocument,
        pageIndex: Int,
        startIndex: Int,
        count: Int
    ): Int {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfDocument.openPage(fromIndex, toIndex)",
        ReplaceWith(
            "pdfDocument.openPage(fromIndex, toIndex)"
        ),
        DeprecationLevel.ERROR
    )
    fun openPage(pdfDocument: PdfDocument, fromIndex: Int, toIndex: Int): LongArray? {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.getPageWidth()",
        ReplaceWith(
            "page.getPageWidth()"
        ),
        DeprecationLevel.ERROR
    )
    fun getPageWidth(doc: PdfDocument, index: Int): Int {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.getPageHeight()",
        ReplaceWith(
            "page.getPageHeight()"
        ),
        DeprecationLevel.ERROR
    )
    fun getPageHeight(doc: PdfDocument, index: Int): Int {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.getPageSize()",
        ReplaceWith(
            "page.getPageSize()"
        ),
        DeprecationLevel.ERROR
    )
    fun getPageSize(doc: PdfDocument, index: Int): Size? {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.renderPage(surface, startX, startY, drawSizeX, drawSizeY)",
        ReplaceWith(
            "page.renderPage(surface, startX, startY, drawSizeX, drawSizeY)"
        ),
        DeprecationLevel.ERROR
    )
    @Suppress("LongParameterList")
    fun renderPage(
        doc: PdfDocument,
        surface: Surface?,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int
    ) {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.renderPage(surface, startX, startY, drawSizeX, drawSizeY, renderAnnot)",
        ReplaceWith(
            "page.renderPage(surface, startX, startY, drawSizeX, drawSizeY, renderAnnot)"
        ),
        DeprecationLevel.ERROR
    )
    @Suppress("LongParameterList")
    fun renderPage(
        doc: PdfDocument,
        surface: Surface?,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean
    ) {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY)",
        ReplaceWith(
            "page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY)"
        ),
        DeprecationLevel.ERROR
    )
    @Suppress("LongParameterList")
    fun renderPageBitmap(
        doc: PdfDocument,
        bitmap: Bitmap?,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int
    ) {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot)",
        ReplaceWith(
            "page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot)"
        ),
        DeprecationLevel.ERROR
    )
    @Suppress("LongParameterList")
    fun renderPageBitmap(
        doc: PdfDocument,
        bitmap: Bitmap?,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean
    ) {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.getPageLinks()",
        ReplaceWith(
            "page.getPageLinks()"
        ),
        DeprecationLevel.ERROR
    )
    @Suppress("LongParameterList")
    fun getPageLinks(doc: PdfDocument, pageIndex: Int): List<PdfDocument.Link>? {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)",
        ReplaceWith(
            "page.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)"
        ),
        DeprecationLevel.ERROR
    )
    @Suppress("LongParameterList")
    fun mapPageCoordsToDevice(
        doc: PdfDocument,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        pageX: Double,
        pageY: Double
    ): Point {
        error("Method has been moved")
    }

    @Deprecated(
        "Use PdfPage.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords)",
        ReplaceWith(
            "page.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords)",
        ),
        DeprecationLevel.ERROR,
    )
    @Suppress("LongParameterList")
    fun mapRectToDevice(
        doc: PdfDocument,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: RectF
    ): RectF? {
        error("Method has been moved")
    }

    companion object {
        private val TAG = PdfiumCore::class.java.name
        private val FD_CLASS: Class<*> = FileDescriptor::class.java
        private const val FD_FIELD_NAME = "descriptor"

        init {
            try {
                System.loadLibrary("partition_alloc.cr")
                System.loadLibrary("c++_chrome.cr")
                System.loadLibrary("chrome_zlib.cr")
                System.loadLibrary("absl.cr")
                System.loadLibrary("icuuc.cr")
                System.loadLibrary("pdfium.cr")
                System.loadLibrary("pdfiumandroid")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Native libraries failed to load - $e")
            }
        }

        /* synchronize native methods */
        val lock = Any()
        private var mFdField: Field? = null
        fun getNumFd(fdObj: ParcelFileDescriptor): Int {
            return try {
                if (mFdField == null) {
                    mFdField = FD_CLASS.getDeclaredField(FD_FIELD_NAME)
                    mFdField?.isAccessible = true
                }
                mFdField?.getInt(fdObj.fileDescriptor) ?: -1
            } catch (e: NoSuchFieldException) {
                Log.e(TAG, "getFdField NoSuchFieldException", e)
                -1
            } catch (e: IllegalAccessException) {
                Log.e(TAG, "IllegalAccessException", e)
                -1
            }
        }
    }
}
