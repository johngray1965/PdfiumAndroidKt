package io.legere.pdfiumandroid.jni

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.PdfDocument.Bookmark
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeDocumentTest : BasePDFTest() {
    private val nativeDocument = defaultNativeFactory.getNativeDocument()

    private lateinit var pdfDocument: PdfDocumentU

    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
    }

    @After
    fun tearDown() {
        try {
            // Some test close the document, so we need to catch the exception
            pdfDocument.close()
        } catch (e: Exception) {
            println("Exception: $e")
        }
    }

    @Test
    fun getPageCount() {
        val pageCount = nativeDocument.getPageCount(pdfDocument.mNativeDocPtr)
        Truth.assertThat(pageCount).isEqualTo(4)
    }

    @Test
    fun loadPage() {
        val page = nativeDocument.loadPage(pdfDocument.mNativeDocPtr, 0)

        Truth.assertThat(page).isNotNull()
    }

    @Test
    fun deletePage() {
        var pageCount = nativeDocument.getPageCount(pdfDocument.mNativeDocPtr)
        Truth.assertThat(pageCount).isEqualTo(4)
        nativeDocument.deletePage(pdfDocument.mNativeDocPtr, 0)
        pageCount = nativeDocument.getPageCount(pdfDocument.mNativeDocPtr)
        Truth.assertThat(pageCount).isEqualTo(3)
    }

    @Test
    fun loadPages() {
        val page = nativeDocument.loadPages(pdfDocument.mNativeDocPtr, 0, 3)

        Truth.assertThat(page.size).isEqualTo(4)
    }

    @Test
    fun getDocumentMetaText() {
        val title = nativeDocument.getDocumentMetaText(pdfDocument.mNativeDocPtr, "Title")
        val author = nativeDocument.getDocumentMetaText(pdfDocument.mNativeDocPtr, "Author")
        val subject = nativeDocument.getDocumentMetaText(pdfDocument.mNativeDocPtr, "Subject")
        val keywords = nativeDocument.getDocumentMetaText(pdfDocument.mNativeDocPtr, "Keywords")
        val creator = nativeDocument.getDocumentMetaText(pdfDocument.mNativeDocPtr, "Creator")
        val producer = nativeDocument.getDocumentMetaText(pdfDocument.mNativeDocPtr, "Producer")
        val creationDate = nativeDocument.getDocumentMetaText(pdfDocument.mNativeDocPtr, "CreationDate")
        val modDate = nativeDocument.getDocumentMetaText(pdfDocument.mNativeDocPtr, "ModDate")

        println("Title: $title")
        println("Author: $author")
        println("Subject: $subject")
        println("Keywords: $keywords")
        println("Creator: $creator")
        println("Producer: $producer")
        println("CreationDate: $creationDate")
        println("ModDate: $modDate")

        Truth.assertThat(title).isEqualTo("Document1")
        Truth.assertThat(author).isEmpty()
        Truth.assertThat(subject).isEmpty()
        Truth.assertThat(keywords).isEmpty()
        Truth.assertThat(creator).isEqualTo("PDF reDirect v2")
    }

    internal fun recursiveGetBookmark(
        tree: MutableList<Bookmark>,
        bookmarkPtr: Long,
        level: Long,
    ) {
        val mNativeDocPtr = pdfDocument.mNativeDocPtr
        var levelMutable = level
        val bookmark = Bookmark()
        bookmark.mNativePtr = bookmarkPtr
        bookmark.title = nativeDocument.getBookmarkTitle(bookmarkPtr)
        bookmark.pageIdx = nativeDocument.getBookmarkDestIndex(mNativeDocPtr, bookmarkPtr)
        tree.add(bookmark)
        val child = nativeDocument.getFirstChildBookmark(mNativeDocPtr, bookmarkPtr)
        if (child != 0L && levelMutable < 16) {
            recursiveGetBookmark(bookmark.children, child, levelMutable++)
        }
        val sibling = nativeDocument.getSiblingBookmark(mNativeDocPtr, bookmarkPtr)
        if (sibling != 0L && levelMutable < 16) {
            recursiveGetBookmark(tree, sibling, levelMutable)
        }
    }

    @Test
    fun getFirstChildBookmark() {
        pdfBytes = getPdfBytes("1604.05669v1.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
        val mNativeDocPtr = pdfDocument.mNativeDocPtr

        val bookmarks: MutableList<Bookmark> =
            ArrayList()
        val first = nativeDocument.getFirstChildBookmark(mNativeDocPtr, 0)
        if (first != 0L) {
            recursiveGetBookmark(bookmarks, first, 1)
        }
        println("bookmarks: $bookmarks")
        Truth.assertThat(bookmarks.size).isEqualTo(2)
    }

    @Test
    fun loadTextPage() {
        val pagePtr = nativeDocument.loadPage(pdfDocument.mNativeDocPtr, 0)
        assertThat(pagePtr).isNotNull()
    }

    @Test
    fun getPageCharCounts() {
        val pageCharCounts = pdfDocument.getPageCharCounts()

        val expectedValues = intArrayOf(3468, 3723, 3966, 2290)
        println(pageCharCounts)

        Truth.assertThat(pageCharCounts).isEqualTo(expectedValues)
    }
}
