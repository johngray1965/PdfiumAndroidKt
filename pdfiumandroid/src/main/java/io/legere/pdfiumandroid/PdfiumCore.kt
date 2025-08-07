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
import io.legere.pdfiumandroid.util.Config
import io.legere.pdfiumandroid.util.InitLock
import io.legere.pdfiumandroid.util.PdfiumNativeSourceBridge
import io.legere.pdfiumandroid.util.Size
import io.legere.pdfiumandroid.util.pdfiumConfig
import kotlinx.coroutines.sync.Mutex
import java.io.IOException

/**
 * PdfiumCore is the main entry-point for access to the PDFium API.
 */
@Suppress("TooManyFunctions")
class PdfiumCore(
    context: Context? = null,
    val config: Config = Config(),
) {
    private val mCurrentDpi: Int

    init {
        pdfiumConfig = config
        Logger.setLogger(config.logger)
        Logger.d(TAG, "Starting PdfiumAndroid ")
        mCurrentDpi = context?.resources?.displayMetrics?.densityDpi ?: -1
        isReady.waitForReady()
    }

    private external fun nativeOpenDocument(
        fd: Int,
        password: String?,
    ): Long

    private external fun nativeOpenMemDocument(
        data: ByteArray?,
        password: String?,
    ): Long

    private external fun nativeOpenCustomDocument(
        data: PdfiumNativeSourceBridge,
        password: String?,
        size: Long,
    ): Long

    /**
     * Create new document from file
     * @param fd opened file descriptor of file
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor): PdfDocument = newDocument(fd, null)

    /**
     * Create new document from file with password
     * @param parcelFileDescriptor opened file descriptor of file
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(
        parcelFileDescriptor: ParcelFileDescriptor,
        password: String?,
    ): PdfDocument {
        synchronized(lock) {
            return PdfDocument(nativeOpenDocument(parcelFileDescriptor.fd, password)).also { document ->
                document.parcelFileDescriptor = parcelFileDescriptor
                document.source = null
            }
        }
    }

    /**
     * Create new document from bytearray
     * @param data bytearray of pdf file
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(data: ByteArray?): PdfDocument = newDocument(data, null)

    /**
     * Create new document from bytearray with password
     * @param data bytearray of pdf file
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(
        data: ByteArray?,
        password: String?,
    ): PdfDocument {
        synchronized(lock) {
            return PdfDocument(nativeOpenMemDocument(data, password)).also { document ->
                document.parcelFileDescriptor = null
                document.source = null
            }
        }
    }

    /**
     * Create new document from custom data source
     * @param data custom data source to read from
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(data: PdfiumSource): PdfDocument = newDocument(data, null)

    /**
     * Create new document from custom data source with password
     * @param data custom data source to read from
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(
        data: PdfiumSource,
        password: String?,
    ): PdfDocument {
        synchronized(lock) {
            val nativeSourceBridge = PdfiumNativeSourceBridge(data)
            return PdfDocument(nativeOpenCustomDocument(nativeSourceBridge, password, data.length)).also { document ->
                document.parcelFileDescriptor = null
                document.source = data
            }
        }
    }

    @Deprecated("Use PdfDocument.getPageCount()", ReplaceWith("pdfDocument.getPageCount()"), DeprecationLevel.WARNING)
    fun getPageCount(pdfDocument: PdfDocument): Int = pdfDocument.getPageCount()

    @Deprecated("Use PdfDocument.closeDocument()", ReplaceWith("pdfDocument.close()"), DeprecationLevel.WARNING)
    fun closeDocument(pdfDocument: PdfDocument) {
        pdfDocument.close()
    }

    @Deprecated(
        "Use PdfDocument.getTableOfContents()",
        ReplaceWith("pdfDocument.getTableOfContents()"),
        DeprecationLevel.WARNING,
    )
    fun getTableOfContents(pdfDocument: PdfDocument): List<PdfDocument.Bookmark> = pdfDocument.getTableOfContents()

    @Suppress("UNUSED_PARAMETER") // Need to keep for compatibility
    @Deprecated(
        "Use PdfDocument.openTextPage()",
        ReplaceWith("pdfDocument.openTextPage(pageIndex)"),
        DeprecationLevel.WARNING,
    )
    fun openTextPage(pdfDocument: PdfDocument, pageIndex: Int): Long = pageIndex.toLong()

    @Suppress("UNUSED_PARAMETER") // Need to keep for compatibility
    @Deprecated(
        "Use PdfDocument.openPage()",
        ReplaceWith("pdfDocument.openPage(pageIndex)"),
        DeprecationLevel.WARNING,
    )
    fun openPage(pdfDocument: PdfDocument, pageIndex: Int): Long = pageIndex.toLong()

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
            return page.getPageMediaBox()
        }
    }

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

    @Suppress("UNUSED_PARAMETER", "EmptyMethod") // Need to keep for compatibility
    @Deprecated(
        "Use textPage.close()",
        ReplaceWith("textPage.close()"),
        DeprecationLevel.ERROR,
    )
    fun closeTextPage(pdfDocument: PdfDocument, pageIndex: Int) {
        // empty
    }

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
            page.openTextPage().use { textPage ->
                return textPage.textPageCountChars()
            }
        }
    }

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
            page.openTextPage().use { textPage ->
                return textPage.textPageGetText(start, count)
            }
        }
    }

    @Deprecated(
        "Use pdfDocument.getDocumentMeta()",
        ReplaceWith("pdfDocument.getDocumentMeta()"),
        DeprecationLevel.WARNING,
    )
    fun getDocumentMeta(pdfDocument: PdfDocument): PdfDocument.Meta = pdfDocument.getDocumentMeta()

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
            return page.getPageWidthPoint()
        }
    }

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
            return page.getPageHeightPoint()
        }
    }

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
            page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot, textMask)
        }
    }

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
            page.openTextPage().use { textPage ->
                return textPage.textPageGetRect(index)
            }
        }
    }

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
            page.openTextPage().use { textPage ->
                return textPage.textPageGetBoundedText(sourceRect, size)
            }
        }
    }

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
            return page.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)
        }
    }

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
            page.openTextPage().use { textPage ->
                return textPage.textPageCountRects(startIndex, count)
            }
        }
    }

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

    @Deprecated(
        "Use PdfPage.getPageWidth()",
        ReplaceWith(
            "page.getPageWidth()",
        ),
        DeprecationLevel.WARNING,
    )
    fun getPageWidth(
        pdfDocument: PdfDocument,
        index: Int,
    ): Int {
        pdfDocument.openPage(index).use { page ->
            return page.getPageWidth(mCurrentDpi)
        }
    }

    @Deprecated(
        "Use PdfPage.getPageHeight()",
        ReplaceWith(
            "page.getPageHeight()",
        ),
        DeprecationLevel.WARNING,
    )
    fun getPageHeight(
        pdfDocument: PdfDocument,
        index: Int,
    ): Int {
        pdfDocument.openPage(index).use { page ->
            return page.getPageHeight(mCurrentDpi)
        }
    }

    @Deprecated(
        "Use PdfPage.getPageSize()",
        ReplaceWith(
            "page.getPageSize()",
        ),
        DeprecationLevel.WARNING,
    )
    fun getPageSize(
        pdfDocument: PdfDocument,
        index: Int,
    ): Size {
        pdfDocument.openPage(index).use { page ->
            return page.getPageSize(mCurrentDpi)
        }
    }

    @Deprecated(
        "Use PdfPage.renderPage(surface, startX, startY, drawSizeX, drawSizeY)",
        ReplaceWith(
            "page.renderPage(surface, startX, startY, drawSizeX, drawSizeY)",
        ),
        DeprecationLevel.WARNING,
    )
    @Suppress("LongParameterList", "ComplexCondition")
    fun renderPage(
        pdfDocument: PdfDocument,
        surface: Surface?,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false,
    ): Boolean {
        var retValue = false
        pdfDocument.openPage(pageIndex).use { page ->
            val sizes = IntArray(2)
            val pointers = LongArray(2)
            surface
                ?.let {
                    PdfPage.lockSurface(
                        it,
                        sizes,
                        pointers,
                    )
                }
            val nativeWindow = pointers[0]
            val bufferPtr = pointers[1]
            if (bufferPtr == 0L || bufferPtr == -1L || nativeWindow == 0L || nativeWindow == -1L) {
                return@use
            }
            retValue = page.renderPage(bufferPtr, startX, startY, drawSizeX, drawSizeY, renderAnnot)
            surface?.let {
                PdfPage.unlockSurface(pointers)
            }
        }
        return retValue
    }

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
            page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot)
        }
    }

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
    ): List<PdfDocument.Link> {
        pdfDocument.openPage(pageIndex).use { page ->
            return page.getPageLinks()
        }
    }

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
        coords: RectF,
    ): Rect {
        pdfDocument.openPage(pageIndex).use { page ->
            return page.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords)
        }
    }

    companion object {
        private val TAG = PdfiumCore::class.java.name

        // synchronize native methods
        val lock = Any()

        val surfaceMutex = Mutex()

        val isReady = InitLock()

        init {
            Log.d(TAG, "init")
            Thread {
                Log.d(TAG, "init thread start")
                synchronized(lock) {
                    Log.d(TAG, "init in lock")
                    try {
                        System.loadLibrary("pdfium")
                        System.loadLibrary("pdfiumandroid")
                        isReady.markReady()
                    } catch (e: UnsatisfiedLinkError) {
                        Logger.e(TAG, e, "Native libraries failed to load")
                    }
                    Log.d(TAG, "init in lock")
                }
            }.start()
        }
    }
}
