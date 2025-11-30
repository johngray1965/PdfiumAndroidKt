package io.legere.pdfiumandroid

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@Suppress("DEPRECATION")
@ExtendWith(MockKExtension::class)
class PdfiumCoreTest {
    lateinit var pdfiumCore: PdfiumCore

    @MockK
    lateinit var pdfiumCoreU: PdfiumCoreU

    @MockK lateinit var document: PdfDocument

    @MockK lateinit var page: PdfPage

    @MockK lateinit var textPage: PdfTextPage

    @BeforeEach
    fun setUp() {
        pdfiumCore = PdfiumCore(coreInternal = pdfiumCoreU)
    }

    @Test
    fun newDocument() {
        val document = mockk<PdfDocumentU>()
        every { pdfiumCoreU.newDocument(any() as ParcelFileDescriptor, any()) } returns document
        val result = pdfiumCore.newDocument(mockk<ParcelFileDescriptor>())
        assertThat(result).isEqualTo(document)
        verify { pdfiumCoreU.newDocument(any() as ParcelFileDescriptor, any()) }
    }

    @Test
    fun testNewDocument() {
    }

    @Test
    fun testNewDocument1() {
        val document = mockk<PdfDocumentU>()
        val data = ByteArray(10)
        every { pdfiumCoreU.newDocument(any() as ByteArray, any()) } returns document
        val result = pdfiumCore.newDocument(data)
        assertThat(result.document).isEqualTo(document)
        verify { pdfiumCoreU.newDocument(any() as ByteArray, any()) }
    }

    @Test
    fun testNewDocument2() {
    }

    @Test
    fun testNewDocument3() {
        val document = mockk<PdfDocumentU>()
        every { pdfiumCoreU.newDocument(any() as PdfiumSource, any()) } returns document
        val result = pdfiumCore.newDocument(mockk<PdfiumSource>())
        assertThat(result.document).isEqualTo(document)
        verify { pdfiumCoreU.newDocument(any() as PdfiumSource, any()) }
    }

    @Test
    fun testNewDocument4() {
    }

    @Test
    fun getPageCount() {
        every { document.getPageCount() } returns 42
        val result = pdfiumCore.getPageCount(document)
        assertThat(result).isEqualTo(42)
        verify { document.getPageCount() }
    }

    @Test
    fun closeDocument() {
        every { document.close() } returns Unit
        pdfiumCore.closeDocument(document)
        verify { document.close() }
    }

    @Test
    fun getTableOfContents() {
        val bookmarks = listOf(mockk<PdfDocument.Bookmark>())
        every { pdfiumCore.getTableOfContents(document) } returns bookmarks
        val result = pdfiumCore.getTableOfContents(document)
        assertThat(result).isEqualTo(bookmarks)
        verify { pdfiumCore.getTableOfContents(document) }
    }

    @Test
    fun openTextPage() {
        pdfiumCore.openTextPage(document, 0)
    }

    @Test
    fun openPage() {
        pdfiumCore.openPage(document, 0)
    }

    @Test
    fun testOpenPage() {
        pdfiumCore.openTextPage(document, 0)
    }

    @Test
    fun getPageMediaBox() {
        val mediaBox = mockk<RectF>()
        every { document.openPage(any()) } returns page
        every { page.getPageMediaBox() } returns mediaBox
        every { page.close() } just runs
        val result = pdfiumCore.getPageMediaBox(document, 0)
        assertThat(result).isEqualTo(mediaBox)
        verify { page.getPageMediaBox() }
        verify { page.close() }
        verify { document.openPage(any()) }
    }

//    @Test
//    fun closePage() {
//        pdfiumCore.closePage(mockk(), 0)
//    }
//
//    @Test
//    fun closeTextPage() {
//        pdfiumCore.closeTextPage(mockk(), 0)
//    }

    @Test
    fun textPageCountChars() {
        every { document.openPage(any()) } returns page
        every { page.openTextPage() } returns textPage
        every { textPage.textPageCountChars() } returns 1234
        every { page.close() } just runs
        every { textPage.close() } just runs
        val result = pdfiumCore.textPageCountChars(document, 0)
        assertThat(result).isEqualTo(1234)
        verify { document.openPage(any()) }
        verify { page.openTextPage() }
        verify { textPage.textPageCountChars() }
        verify { page.close() }
        verify { textPage.close() }
    }

    @Test
    fun textPageGetText() {
        every { document.openPage(any()) } returns page
        every { page.openTextPage() } returns textPage
        every { textPage.textPageGetText(any(), any()) } returns "Yo"
        every { page.close() } just runs
        every { textPage.close() } just runs
        val result = pdfiumCore.textPageGetText(document, 0, 0, 0)
        assertThat(result).isEqualTo("Yo")
        verify { document.openPage(any()) }
        verify { page.openTextPage() }
        verify { textPage.textPageGetText(any(), any()) }
        verify { page.close() }
        verify { textPage.close() }
    }

    @Test
    fun getDocumentMeta() {
        val meta = mockk<PdfDocument.Meta>()
        every { pdfiumCore.getDocumentMeta(document) } returns meta
        val results = pdfiumCore.getDocumentMeta(document)
        assertThat(results).isEqualTo(meta)
        verify { pdfiumCore.getDocumentMeta(document) }
    }

    @Test
    fun getPageWidthPoint() {
        every { document.openPage(any()) } returns page
        every { page.getPageWidthPoint() } returns 123
        every { page.close() } just runs
        val result = pdfiumCore.getPageWidthPoint(document, 0)
        assertThat(result).isEqualTo(123)
        verify { page.getPageWidthPoint() }
        verify { page.close() }
        verify { document.openPage(any()) }
    }

    @Test
    fun getPageHeightPoint() {
        every { document.openPage(any()) } returns page
        every { page.getPageHeightPoint() } returns 123
        every { page.close() } just runs
        val result = pdfiumCore.getPageHeightPoint(document, 0)
        assertThat(result).isEqualTo(123)
        verify { page.getPageHeightPoint() }
        verify { page.close() }
        verify { document.openPage(any()) }
    }

    @Test
    fun renderPageBitmap() {
        every { document.openPage(any()) } returns page
        every { page.renderPageBitmap(any<Bitmap>(), any<Int>(), any<Int>(), any<Int>(), any<Int>(), any<Boolean>()) } just runs
        every { page.close() } just runs
        pdfiumCore.renderPageBitmap(document, mockk(), 0, 0, 0, 0, 0)
        verify { page.renderPageBitmap(any<Bitmap>(), any<Int>(), any<Int>(), any<Int>(), any<Int>(), any<Boolean>()) }
        verify { page.close() }
        verify { document.openPage(any()) }
    }

    @Test
    fun testRenderPageBitmap() {
        every { document.openPage(any()) } returns page
        every { page.renderPageBitmap(any<Bitmap>(), any<Int>(), any<Int>(), any<Int>(), any<Int>(), any<Boolean>()) } just runs
        every { page.close() } just runs
        pdfiumCore.renderPageBitmap(document, mockk(), 0, 0, 0, 0, 0)
        verify { page.renderPageBitmap(any<Bitmap>(), any<Int>(), any<Int>(), any<Int>(), any<Int>(), any<Boolean>()) }
        verify { page.close() }
        verify { document.openPage(any()) }
    }

    @Test
    fun textPageGetRect() {
        val rect = mockk<RectF>()
        every { document.openPage(any()) } returns page
        every { page.openTextPage() } returns textPage
        every { textPage.textPageGetRect(any()) } returns rect
        every { page.close() } just runs
        every { textPage.close() } just runs
        val result = pdfiumCore.textPageGetRect(document, 0, 0)
        assertThat(result).isEqualTo(rect)
        verify { document.openPage(any()) }
        verify { page.openTextPage() }
        verify { textPage.textPageGetRect(any()) }
        verify { page.close() }
        verify { textPage.close() }
    }

    @Test
    fun textPageGetBoundedText() {
        every { document.openPage(any()) } returns page
        every { page.openTextPage() } returns textPage
        every { textPage.textPageGetBoundedText(any(), any()) } returns "Yo"
        every { page.close() } just runs
        every { textPage.close() } just runs
        val result = pdfiumCore.textPageGetBoundedText(document, 0, mockk(), 0)
        assertThat(result).isEqualTo("Yo")
        verify { document.openPage(any()) }
        verify { page.openTextPage() }
        verify { textPage.textPageGetBoundedText(any(), any()) }
        verify { page.close() }
        verify { textPage.close() }
    }

    @Test
    fun mapRectToPage() {
        val expected = mockk<RectF>()
        every { document.openPage(any()) } returns page
        every { page.mapRectToPage(any(), any(), any(), any(), any(), any()) } returns expected
        every { page.close() } just runs
        val result = pdfiumCore.mapRectToPage(document, 0, 0, 0, 0, 0, 0, mockk())
        assertThat(result).isEqualTo(expected)
        verify { page.mapRectToPage(any(), any(), any(), any(), any(), any()) }
        verify { page.close() }
        verify { document.openPage(any()) }
    }

    @Test
    fun textPageCountRects() {
        every { document.openPage(any()) } returns page
        every { page.openTextPage() } returns textPage
        every { textPage.textPageCountRects(any(), any()) } returns 124
        every { page.close() } just runs
        every { textPage.close() } just runs
        val result = pdfiumCore.textPageCountRects(document, 0, 0, 0)
        assertThat(result).isEqualTo(124)
        verify { document.openPage(any()) }
        verify { page.openTextPage() }
        verify { textPage.textPageCountRects(any(), any()) }
        verify { page.close() }
        verify { textPage.close() }
    }

    @Test
    fun getPageLinks() {
        val expected = listOf(mockk<PdfDocument.Link>())
        every { document.openPage(any()) } returns page
        every { page.getPageLinks() } returns expected
        every { page.close() } just runs
        val result = pdfiumCore.getPageLinks(document, 0)
        assertThat(result).isEqualTo(expected)
        verify { page.getPageLinks() }
        verify { page.close() }
        verify { document.openPage(any()) }
    }

    @Test
    fun mapPageCoordsToDevice() {
        val expected = mockk<Point>()
        every { document.openPage(any()) } returns page
        every { page.mapPageCoordsToDevice(any(), any(), any(), any(), any(), any(), any()) } returns expected
        every { page.close() } just runs
        val result = pdfiumCore.mapPageCoordsToDevice(document, 0, 0, 0, 0, 0, 0, 0.0, 0.0)
        assertThat(result).isEqualTo(expected)
        verify { page.mapPageCoordsToDevice(any(), any(), any(), any(), any(), any(), any()) }
        verify { page.close() }
        verify { document.openPage(any()) }
    }

    @Test
    fun mapRectToDevice() {
        val expected = mockk<Rect>()
        every { document.openPage(any()) } returns page
        every { page.mapRectToDevice(any(), any(), any(), any(), any(), any()) } returns expected
        every { page.close() } just runs
        val result = pdfiumCore.mapRectToDevice(document, 0, 0, 0, 0, 0, 0, mockk())
        assertThat(result).isEqualTo(expected)
        verify { page.mapRectToDevice(any(), any(), any(), any(), any(), any()) }
        verify { page.close() }
        verify { document.openPage(any()) }
    }
}
