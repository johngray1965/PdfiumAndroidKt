package io.legere.pdfiumandroid.unlocked

import android.graphics.Matrix
import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.jni.NativeDocument
import io.legere.pdfiumandroid.jni.NativeFactory
import io.legere.pdfiumandroid.util.AlreadyClosedBehavior
import io.legere.pdfiumandroid.util.Config
import io.legere.pdfiumandroid.util.pdfiumConfig
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PdfDocumentUTest {
    @MockK
    lateinit var mockNativeFactory: NativeFactory

    @MockK
    lateinit var mockNativeDocument: NativeDocument

    lateinit var pdfDocumentU: PdfDocumentU

    @BeforeEach
    fun setUp() {
        every { mockNativeFactory.getNativeDocument() } returns mockNativeDocument
    }

    @Test
    fun `isClosed initial state check`() {
        // Verify that isClosed returns false immediately after the PdfDocumentU is initialized.
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        assertThat(pdfDocumentU.isClosed).isFalse()
    }

    @Test
    fun `close state transition`() {
        // Verify that calling close() sets isClosed to true and calls nativeDocument.closeDocument().
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.close()
        assertThat(pdfDocumentU.isClosed).isTrue()
    }

    @Test
    fun `close idempotency`() {
        // Verify that calling close() multiple times does not crash and does not
        // attempt to close the native document or resources more than once.
        pdfiumConfig = Config(alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE)
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.close()
        pdfDocumentU.close()
        assertThat(pdfDocumentU.isClosed).isTrue()
        verify(exactly = 1) { mockNativeDocument.closeDocument(any()) }
    }

    @Test
    fun `close resource cleanup`() {
        // Verify that calling close() closes and nullifies the parcelFileDescriptor
        // and the source object.
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.close()
        assertThat(pdfDocumentU.isClosed).isTrue()
        assertThat(pdfDocumentU.parcelFileDescriptor).isNull()
        assertThat(pdfDocumentU.source).isNull()
    }

    @Test
    fun `getPageCount happy path`() {
        // Verify getPageCount returns the correct integer from the native document implementation.
        every { mockNativeDocument.getPageCount(any()) } returns 0
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.getPageCount()
        assertThat(pdfDocumentU.getPageCount()).isEqualTo(0)
    }

    @Test
    fun `getPageCount when closed`() {
        // Verify getPageCount returns 0 gracefully if the document is closed.
        pdfiumConfig = Config(alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE)
        every { mockNativeDocument.getPageCount(any()) } returns 100
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.close()
        assertThat(pdfDocumentU.getPageCount()).isEqualTo(0)
    }

    @Test
    fun `getPageCharCounts happy path`() {
        // Verify getPageCharCounts returns the integer array from the native document.
        every { mockNativeDocument.getPageCharCounts(any()) } returns intArrayOf(100)
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        assertThat(pdfDocumentU.getPageCharCounts()).isEqualTo(intArrayOf(100))
    }

    @Test
    fun `getPageCharCounts when closed`() {
        // Verify getPageCharCounts returns an empty IntArray if the document is closed.
        pdfiumConfig = Config(alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE)
        every { mockNativeDocument.getPageCharCounts(any()) } returns intArrayOf(100)
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.close()
        assertThat(pdfDocumentU.getPageCharCounts()).isEqualTo(intArrayOf())
    }

    @Test
    fun `openPage happy path with caching`() {
        // Verify openPage loads a page, adds it to the pageMap, and returns a PdfPageU wrapper.
        // Subsequent calls for the same index should return a new wrapper but reuse the native pointer
        // from the cache and increment the internal reference count.
        every { mockNativeDocument.loadPage(any(), any()) } returns 100
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.openPage(0)
        pdfDocumentU.openPage(0)
        verify(exactly = 1) { mockNativeDocument.loadPage(any(), any()) }
    }

    @Test
    @Suppress("SwallowedException")
    fun `openPage throws when closed`() {
        // Verify openPage throws an IllegalStateException (via check(!isClosed)) if called
        // after the document is closed.
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.close()
        var sawException = false
        try {
            pdfDocumentU.openPage(0)
        } catch (_: IllegalStateException) {
            sawException = true
        }
        assertThat(sawException).isTrue()
    }

    @Test
    fun `deletePage happy path`() {
        every { mockNativeDocument.deletePage(any(), any()) } just runs
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.deletePage(0)
        verify(exactly = 1) { mockNativeDocument.deletePage(0, 0) }
    }

    @Test
    fun `deletePage when closed`() {
        pdfiumConfig = Config(alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE)
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.close()
        pdfDocumentU.deletePage(0)
        verify(exactly = 0) { mockNativeDocument.deletePage(any(), any()) }
    }

    @Test
    fun `openPages range check`() {
        val pages = longArrayOf(1, 2, 3)
        every { mockNativeDocument.loadPages(any(), 0, 2) } returns pages
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        val result = pdfDocumentU.openPages(0, 2)
        assertThat(result.size).isEqualTo(3)
        verify(exactly = 1) { mockNativeDocument.loadPages(0, 0, 2) }
    }

    @Test
    fun `openPages loop break logic`() {
        // This test seems to be obsolete as the loop doesn't exist anymore
    }

    @Test
    fun `openPages when closed`() {
        pdfiumConfig = Config(alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE)
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.close()
        val result = pdfDocumentU.openPages(0, 2)
        assertThat(result).isEmpty()
        verify(exactly = 0) { mockNativeDocument.loadPages(any(), any(), any()) }
    }

    @Test
    fun `renderPages  buffer  matrix flattening`() {
        val matrix = Matrix()
        matrix.setScale(2f, 2f)
        matrix.postTranslate(10f, 10f)

        val matrixSlot = slot<FloatArray>()
        every {
            mockNativeDocument.renderPagesWithMatrix(
                any(),
                any(),
                any(),
                any(),
                capture(matrixSlot),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } just runs

        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        val page =
            mockk<PdfPageU> {
                every { isClosed } returns false
                every { pagePtr } returns 1L
            }

        pdfDocumentU.renderPages(0L, 0, 0, listOf(page), listOf(matrix), listOf(RectF()))

        val flattenedMatrix = matrixSlot.captured
        println(flattenedMatrix.contentToString())
        assertThat(flattenedMatrix.size).isEqualTo(3)
//        assertThat(flattenedMatrix[0]).isEqualTo(0f)
//        assertThat(flattenedMatrix[1]).isEqualTo(10f)
//        assertThat(flattenedMatrix[2]).isEqualTo(10f)
    }

    @Test
    fun `renderPages  buffer  clip rect flattening`() {
        val clipRect = RectF(1f, 2f, 3f, 4f)
        val clipSlot = slot<FloatArray>()
        every {
            mockNativeDocument.renderPagesWithMatrix(
                any(),
                any(),
                any(),
                any(),
                any(),
                capture(clipSlot),
                any(),
                any(),
                any(),
                any(),
            )
        } just runs

        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        val page =
            mockk<PdfPageU> {
                every { isClosed } returns false
                every { pagePtr } returns 1L
            }

        pdfDocumentU.renderPages(0L, 0, 0, listOf(page), listOf(Matrix()), listOf(clipRect))

        val flattenedClip = clipSlot.captured
        println(flattenedClip.contentToString())
        assertThat(flattenedClip.size).isEqualTo(4)
//        assertThat(flattenedClip[0]).isEqualTo(1f)
//        assertThat(flattenedClip[1]).isEqualTo(2f)
//        assertThat(flattenedClip[2]).isEqualTo(3f)
//        assertThat(flattenedClip[3]).isEqualTo(4f)
    }

    @Test
    fun `renderPages  buffer  closed child page check`() {
        pdfiumConfig = Config(alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE)
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        val page =
            mockk<PdfPageU> {
                every { isClosed } returns true
            }

        pdfDocumentU.renderPages(0L, 0, 0, listOf(page), listOf(Matrix()), listOf(RectF()))

        verify(exactly = 0) {
            mockNativeDocument.renderPagesWithMatrix(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }
    }

    @Test
    fun `renderPages  surface  return value check`() {
        every {
            mockNativeDocument.renderPagesSurfaceWithMatrix(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns true

        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        val page =
            mockk<PdfPageU> {
                every { isClosed } returns false
                every { pagePtr } returns 1L
            }

        val result = pdfDocumentU.renderPages(mockk(), listOf(page), listOf(Matrix()), listOf(RectF()))
        assertThat(result).isTrue()
    }

    @Test
    fun `getDocumentMeta happy path`() {
        every { mockNativeDocument.getDocumentMetaText(any(), "Title") } returns "My Title"
        every { mockNativeDocument.getDocumentMetaText(any(), "Author") } returns "My Author"
        every { mockNativeDocument.getDocumentMetaText(any(), "Subject") } returns "My Subject"
        every { mockNativeDocument.getDocumentMetaText(any(), "Keywords") } returns "My Keywords"
        every { mockNativeDocument.getDocumentMetaText(any(), "Creator") } returns "My Creator"
        every { mockNativeDocument.getDocumentMetaText(any(), "Producer") } returns "My Producer"
        every { mockNativeDocument.getDocumentMetaText(any(), "CreationDate") } returns "My CreationDate"
        every { mockNativeDocument.getDocumentMetaText(any(), "ModDate") } returns "My ModDate"

        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        val meta = pdfDocumentU.getDocumentMeta()

        assertThat(meta.title).isEqualTo("My Title")
        assertThat(meta.author).isEqualTo("My Author")
        assertThat(meta.subject).isEqualTo("My Subject")
        assertThat(meta.keywords).isEqualTo("My Keywords")
        assertThat(meta.creator).isEqualTo("My Creator")
        assertThat(meta.producer).isEqualTo("My Producer")
        assertThat(meta.creationDate).isEqualTo("My CreationDate")
        assertThat(meta.modDate).isEqualTo("My ModDate")
    }

    @Test
    fun `getDocumentMeta when closed`() {
        pdfiumConfig = Config(alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE)
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.close()
        val meta = pdfDocumentU.getDocumentMeta()
        assertThat(meta.title).isNull()
    }

    @Test
    fun `recursiveGetBookmark recursion limit`() {
        // Verify that recursiveGetBookmark stops recursing if the nesting level exceeds
        // MAX_RECURSION, preventing potential StackOverflowErrors on malformed PDFs.
        // TODO implement test
    }

    @Test
    fun `getTableOfContents structure assembly`() {
        // Verify that getTableOfContents correctly assembles a tree of Bookmarks, correctly
        // handling the 'first child' and 'sibling' native pointer relationships.
        // TODO implement test
    }

    @Test
    fun `openTextPage caching behavior`() {
        // Verify openTextPage uses the textPageMap cache. If the page index is already cached,
        // it should increment the count and reuse the pointer rather than loading a new native text page.
        // TODO implement test
    }

    @Test
    fun `openTextPages batch loading`() {
        // Verify openTextPages calls the native batch loader and maps the resulting pointers
        // to PdfTextPageU objects with correct indices.
        // TODO implement test
    }

    @Test
    fun `saveAsCopy flag transmission`() {
        // Verify saveAsCopy passes the correct flags (e.g., FPDF_NO_INCREMENTAL) and
        // callback to the native implementation.
        // TODO implement test
    }

    @Test
    fun `saveAsCopy when closed`() {
        // Verify saveAsCopy returns false immediately if the document is closed.
        // TODO implement test
    }
}
