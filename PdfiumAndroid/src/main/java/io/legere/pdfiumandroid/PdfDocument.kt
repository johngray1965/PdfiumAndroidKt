@file:Suppress("unused")

package io.legere.pdfiumandroid

import android.graphics.RectF
import android.os.ParcelFileDescriptor
import java.io.Closeable


private const val MAX_RECURSION = 16

@Suppress("TooManyFunctions")
class PdfDocument(val mNativeDocPtr: Long, private val mCurrentDpi: Int
) : Closeable {

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


    fun getPageCount(): Int {
        synchronized(PdfiumCore.lock) {
            return nativeGetPageCount(mNativeDocPtr)
        }
    }

    /** Open page and store native pointer in [PdfDocument]  */
    fun openPage(pageIndex: Int): PdfPage {
        synchronized(PdfiumCore.lock) {
            val pagePtr = nativeLoadPage(this.mNativeDocPtr, pageIndex)
            return PdfPage(this, pageIndex, pagePtr, mCurrentDpi)
        }
    }

    /** Open range of pages and store native pointers in [PdfDocument]  */
    fun openPages(fromIndex: Int, toIndex: Int): List<PdfPage> {
        var pagesPtr: LongArray
        synchronized(PdfiumCore.lock) {
            pagesPtr = nativeLoadPages(this.mNativeDocPtr, fromIndex, toIndex)
            var pageIndex = fromIndex
            for (page in pagesPtr) {
                if (pageIndex > toIndex) break
                pageIndex++
            }
            return pagesPtr.map { PdfPage(this, pageIndex, it, mCurrentDpi) }
        }
    }


    /** Get metadata for given document  */
    fun getDocumentMeta(): Meta {
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

    /** Get table of contents (bookmarks) for given document  */
    fun getTableOfContents(): List<Bookmark> {
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


    fun openTextPage(pageIndex: Int): PdfTextPage {
        synchronized(PdfiumCore.lock) {
            val page = openPage(pageIndex)
            val textPagePtr = nativeLoadTextPage(this.mNativeDocPtr, page.pagePtr)
            return PdfTextPage(this, pageIndex, textPagePtr)
        }
    }

    fun openTextPages(fromIndex: Int, toIndex: Int): List<PdfTextPage> {
        var textPagesPtr: LongArray
        synchronized(PdfiumCore.lock) {
            textPagesPtr = nativeLoadPages(mNativeDocPtr, fromIndex, toIndex)
            return textPagesPtr.mapIndexed { index: Int, pagePtr: Long ->
                PdfTextPage(
                    this,
                    fromIndex + index,
                    pagePtr
                )
            }
        }
    }



    fun saveAsCopy(callback: PdfWriteCallback): Boolean {
        return nativeSaveAsCopy(mNativeDocPtr, callback)
    }

    override fun close() {
        synchronized(PdfiumCore.lock) {
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

}
