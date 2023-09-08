@file:Suppress("unused")

package io.legere.pdfiumandroid

import android.graphics.RectF
import android.os.ParcelFileDescriptor
import java.io.Closeable

private const val MAX_RECURSION = 16

/**
 * PdfDocument represents a PDF file and allows you to load pages from it.
 */
@Suppress("TooManyFunctions")
class PdfDocument(
    val mNativeDocPtr: Long // , private val mCurrentDpi: Int*
) : Closeable {

    private val pageMap = mutableMapOf<Int, PageCount>()
    private val textPageMap = mutableMapOf<Int, PageCount>()

    var isClosed = false
        private set

    private external fun nativeGetPageCount(docPtr: Long): Int
    private external fun nativeLoadPage(docPtr: Long, pageIndex: Int): Long
    private external fun nativeCloseDocument(docPtr: Long)
    private external fun nativeLoadPages(docPtr: Long, fromIndex: Int, toIndex: Int): LongArray
    private external fun nativeGetDocumentMetaText(docPtr: Long, tag: String): String
    private external fun nativeGetFirstChildBookmark(docPtr: Long, bookmarkPtr: Long?): Long?
    private external fun nativeGetSiblingBookmark(docPtr: Long, bookmarkPtr: Long): Long?
    private external fun nativeGetBookmarkDestIndex(docPtr: Long, bookmarkPtr: Long): Long
    private external fun nativeLoadTextPage(docPtr: Long, pagePtr: Long): Long
    private external fun nativeGetBookmarkTitle(bookmarkPtr: Long): String
    private external fun nativeSaveAsCopy(docPtr: Long, callback: PdfWriteCallback): Boolean

    var parcelFileDescriptor: ParcelFileDescriptor? = null

    /**
     *  Get the page count of the PDF document
     *  @return the number of pages
     */
    fun getPageCount(): Int {
        check(!isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            return nativeGetPageCount(mNativeDocPtr)
        }
    }

    /**
     * Open page and store native pointer in [PdfDocument]
     * @param pageIndex the page index
     * @return the opened page [PdfPage]
     * @throws IllegalArgumentException if  document is closed or the page cannot be loaded
     */
    fun openPage(pageIndex: Int): PdfPage {
        check(!isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            if (pageMap.containsKey(pageIndex)) {
                pageMap[pageIndex]?.let {
                    it.count++
                    return PdfPage(this, pageIndex, it.pagePtr, pageMap)
                }
            }
            val pagePtr = nativeLoadPage(this.mNativeDocPtr, pageIndex)
            pageMap[pageIndex] = PageCount(pagePtr, 1)
            return PdfPage(this, pageIndex, pagePtr, pageMap)
        }
    }

    /**
     * Open range of pages and store native pointers in [PdfDocument]
     * @param fromIndex the start index of the range
     * @param toIndex the end index of the range
     * @return the opened pages [PdfPage]
     * @throws IllegalArgumentException if document is closed or the pages cannot be loaded
     */
    fun openPages(fromIndex: Int, toIndex: Int): List<PdfPage> {
        check(!isClosed) { "Already closed" }
        var pagesPtr: LongArray
        synchronized(PdfiumCore.lock) {
            pagesPtr = nativeLoadPages(this.mNativeDocPtr, fromIndex, toIndex)
            var pageIndex = fromIndex
            for (page in pagesPtr) {
                if (pageIndex > toIndex) break
                pageIndex++
            }
            return pagesPtr.map { PdfPage(this, pageIndex, it, pageMap) }
        }
    }

    /**
     * Get metadata for given document
     * @return the [Meta] data
     * @throws IllegalArgumentException if document is closed
     */
    fun getDocumentMeta(): Meta {
        check(!isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            val meta = Meta()
            meta.title = nativeGetDocumentMetaText(mNativeDocPtr, "Title")
            meta.author = nativeGetDocumentMetaText(mNativeDocPtr, "Author")
            meta.subject = nativeGetDocumentMetaText(mNativeDocPtr, "Subject")
            meta.keywords = nativeGetDocumentMetaText(mNativeDocPtr, "Keywords")
            meta.creator = nativeGetDocumentMetaText(mNativeDocPtr, "Creator")
            meta.producer = nativeGetDocumentMetaText(mNativeDocPtr, "Producer")
            meta.creationDate = nativeGetDocumentMetaText(mNativeDocPtr, "CreationDate")
            meta.modDate = nativeGetDocumentMetaText(mNativeDocPtr, "ModDate")
            return meta
        }
    }

    private fun recursiveGetBookmark(
        tree: MutableList<Bookmark>,
        bookmarkPtr: Long,
        level: Long
    ) {
        check(!isClosed) { "Already closed" }
        var levelMutable = level
        val bookmark = Bookmark()
        bookmark.mNativePtr = bookmarkPtr
        bookmark.title = nativeGetBookmarkTitle(bookmarkPtr)
        bookmark.pageIdx = nativeGetBookmarkDestIndex(mNativeDocPtr, bookmarkPtr)
        tree.add(bookmark)
        val child = nativeGetFirstChildBookmark(mNativeDocPtr, bookmarkPtr)
        if (child != null && levelMutable < MAX_RECURSION) {
            recursiveGetBookmark(bookmark.children, child, levelMutable++)
        }
        val sibling = nativeGetSiblingBookmark(mNativeDocPtr, bookmarkPtr)
        if (sibling != null && levelMutable < MAX_RECURSION) {
            recursiveGetBookmark(tree, sibling, levelMutable)
        }
    }

    /**
     * Get table of contents (bookmarks) for given document
     * @return the [Bookmark] list
     * @throws IllegalArgumentException if document is closed
     */
    fun getTableOfContents(): List<Bookmark> {
        check(!isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            val topLevel: MutableList<Bookmark> =
                ArrayList()
            val first = nativeGetFirstChildBookmark(this.mNativeDocPtr, null)
            if (first != null) {
                recursiveGetBookmark(topLevel, first, 1)
            }
            return topLevel
        }
    }

    /**
     * Open a text page
     * @param page the [PdfPage]
     * @return the opened [PdfTextPage]
     * @throws IllegalArgumentException if document is closed or the page cannot be loaded
     */
    fun openTextPage(page: PdfPage): PdfTextPage {
        check(!isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            if (textPageMap.containsKey(page.pageIndex)) {
                textPageMap[page.pageIndex]?.let {
                    it.count++
                    return PdfTextPage(this, page.pageIndex, it.pagePtr, textPageMap)
                }
            }
            val textPagePtr = nativeLoadTextPage(this.mNativeDocPtr, page.pagePtr)
            textPageMap[page.pageIndex] = PageCount(textPagePtr, 1)
            return PdfTextPage(this, page.pageIndex, textPagePtr, textPageMap)
        }
    }

    /**
     * Open a range of text pages
     * @param fromIndex the start index of the range
     * @param toIndex the end index of the range
     * @return the opened [PdfTextPage] list
     * @throws IllegalArgumentException if document is closed or the pages cannot be loaded
     */
    fun openTextPages(fromIndex: Int, toIndex: Int): List<PdfTextPage> {
        check(!isClosed) { "Already closed" }
        var textPagesPtr: LongArray
        synchronized(PdfiumCore.lock) {
            textPagesPtr = nativeLoadPages(mNativeDocPtr, fromIndex, toIndex)
            return textPagesPtr.mapIndexed { index: Int, pagePtr: Long ->
                PdfTextPage(
                    this,
                    fromIndex + index,
                    pagePtr,
                    textPageMap
                )
            }
        }
    }

    /**
     * Save document as a copy
     * @param callback the [PdfWriteCallback] to be called with the data
     * @return true if the document was successfully saved
     * @throws IllegalArgumentException if document is closed
     */
    fun saveAsCopy(callback: PdfWriteCallback): Boolean {
        check(!isClosed) { "Already closed" }
        return nativeSaveAsCopy(mNativeDocPtr, callback)
    }

    /**
     * Close the document
     * @throws IllegalArgumentException if document is closed
     */
    override fun close() {
        check(!isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            isClosed = true
            nativeCloseDocument(mNativeDocPtr)
            parcelFileDescriptor?.close()
            parcelFileDescriptor = null
        }
    }

    class Meta {
        var title: String? = null
        var author: String? = null
        var subject: String? = null
        var keywords: String? = null
        var creator: String? = null
        var producer: String? = null
        var creationDate: String? = null
        var modDate: String? = null
    }

    class Bookmark {
        val children: MutableList<Bookmark> = ArrayList()
        var title: String? = null
        var pageIdx: Long = 0
        var mNativePtr: Long = 0
    }

    class Link(val bounds: RectF, val destPageIdx: Int?, val uri: String?)

    data class PageCount(val pagePtr: Long, var count: Int)
}
