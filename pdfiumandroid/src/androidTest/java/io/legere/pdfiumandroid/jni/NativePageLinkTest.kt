package io.legere.pdfiumandroid.jni

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.unlocked.PdfPageU
import io.legere.pdfiumandroid.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.StandardCharsets

@RunWith(AndroidJUnit4::class)
class NativePageLinkTest : BasePDFTest() {
    private val nativePage = defaultNativeFactory.getNativePage()
    private val nativeTextPage = defaultNativeFactory.getNativeTextPage()
    private val nativePageLink = defaultNativeFactory.getNativePageLink()
    private lateinit var pdfDocument: PdfDocumentU
    private lateinit var pdfPage: PdfPageU
    private lateinit var pdfTextPage: PdfTextPageU
    private var pdfBytes: ByteArray? = null

    private var pageTextPtr: Long = 0

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("pdf-test.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
        pdfPage = pdfDocument.openPage(0)
        pdfTextPage = pdfPage.openTextPage()
        pageTextPtr = pdfTextPage.pagePtr
    }

    @After
    fun tearDown() {
        pdfTextPage.close()
        pdfPage.close()
        pdfDocument.close()
    }

    @Test
    fun closePageLink() {
        val links = nativeTextPage.loadWebLink(pageTextPtr)
        Truth.assertThat(links).isNotNull()
        nativePageLink.closePageLink(links)
    }

    @Test
    fun countWebLinks() {
        val links = nativeTextPage.loadWebLink(pageTextPtr)
        Truth.assertThat(links).isNotNull()
        val count = nativePageLink.countWebLinks(links)
        Truth.assertThat(count).isEqualTo(1)
        nativePageLink.closePageLink(links)
    }

    @Test
    fun getURL() {
        val links = nativeTextPage.loadWebLink(pageTextPtr)
        Truth.assertThat(links).isNotNull()
        val range = nativePageLink.getTextRange(links, 0)
        val count = range[1]
        val bytes = ByteArray(count * 2)
        nativePageLink.getURL(links, 0, count, bytes)
        val result = String(bytes, StandardCharsets.UTF_16LE)
        Truth.assertThat(result).isEqualTo("http://www.education.gov.yk.ca/")
        nativePageLink.closePageLink(links)
    }

    @Test
    fun countRects() {
        val links = nativeTextPage.loadWebLink(pageTextPtr)
        Truth.assertThat(links).isNotNull()
        val count = nativePageLink.countRects(links, 0)
        Truth.assertThat(count).isEqualTo(1)
        nativePageLink.closePageLink(links)
    }

    @Test
    fun getRect() {
        val links = nativeTextPage.loadWebLink(pageTextPtr)
        Truth.assertThat(links).isNotNull()
        val rect = nativePageLink.getRect(links, 0, 0)
        Truth.assertThat(rect).isEqualTo(floatArrayOf(221.46f, 480.624f, 389.66394f, 469.152f))
        nativePageLink.closePageLink(links)
    }

    @Test
    fun getTextRange() {
        val links = nativeTextPage.loadWebLink(pageTextPtr)
        Truth.assertThat(links).isNotNull()
        val range = nativePageLink.getTextRange(links, 0)
        Truth.assertThat(range).isEqualTo(intArrayOf(351, 31))
        nativePageLink.closePageLink(links)
    }

    @Test
    fun getPageLinks() {
        val links = nativePage.getPageLinks(pdfPage.pagePtr)
        Truth.assertThat(links).isNotNull()
        Truth.assertThat(links.size).isEqualTo(1)
        val link = links[0]
        val uri = nativePage.getLinkURI(pdfDocument.mNativeDocPtr, link)
        Truth.assertThat(uri).isEqualTo("http://www.education.gov.yk.ca/")
        val rect = nativePage.getLinkRect(pdfDocument.mNativeDocPtr, link)
        Truth.assertThat(rect).isEqualTo(floatArrayOf(220.68001f, 483.852f, 389.461f, 467.87997f))
        val page = nativePage.getDestPageIndex(pdfDocument.mNativeDocPtr, link)
        Truth.assertThat(page).isEqualTo(-1)
    }
}
