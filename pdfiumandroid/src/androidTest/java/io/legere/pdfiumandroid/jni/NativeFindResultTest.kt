package io.legere.pdfiumandroid.jni

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.unlocked.PdfPageU
import io.legere.pdfiumandroid.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeFindResultTest : BasePDFTest() {
    private val nativeTextPage = defaultNativeFactory.getNativeTextPage()
    private val nativeFindResult = defaultNativeFactory.getNativeFindResult()
    private lateinit var pdfDocument: PdfDocumentU
    private lateinit var pdfPage: PdfPageU
    private lateinit var pdfTextPage: PdfTextPageU
    private var pdfBytes: ByteArray? = null

    private var pageTextPtr: Long = 0

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
        pdfPage = pdfDocument.openPage(0)!!
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
    fun findNext() {
        val findResult = nativeTextPage.findStart(pageTextPtr, "children's", 0, 0)
        val count = nativeFindResult.getSchCount(findResult)
        println("count: $count")
        var index = nativeFindResult.getSchResultIndex(findResult)
        assertThat(index).isEqualTo(0)
        var hasNext: Boolean = nativeFindResult.findNext(findResult)
        assertThat(hasNext).isTrue()
        index = nativeFindResult.getSchResultIndex(findResult)
        assertThat(index).isEqualTo(1503)
        hasNext = nativeFindResult.findNext(findResult)
        assertThat(hasNext).isTrue()
        index = nativeFindResult.getSchResultIndex(findResult)
        assertThat(index).isEqualTo(2719)
        hasNext = nativeFindResult.findNext(findResult)
        assertThat(hasNext).isFalse()

        nativeFindResult.closeFind(findResult)
    }

    @Test
    fun findPrev() {
        val findResult = nativeTextPage.findStart(pageTextPtr, "children's", 0, 2800)
        val count = nativeFindResult.getSchCount(findResult)
        println("count: $count")
        var index = nativeFindResult.getSchResultIndex(findResult)
        assertThat(index).isEqualTo(0)
        var hasPrev: Boolean = nativeFindResult.findPrev(findResult)
        assertThat(hasPrev).isTrue()
        index = nativeFindResult.getSchResultIndex(findResult)
        assertThat(index).isEqualTo(2719)
        hasPrev = nativeFindResult.findPrev(findResult)
        assertThat(hasPrev).isTrue()
        index = nativeFindResult.getSchResultIndex(findResult)
        assertThat(index).isEqualTo(1503)
        hasPrev = nativeFindResult.findPrev(findResult)
        assertThat(hasPrev).isFalse()

        nativeFindResult.closeFind(findResult)
    }
}
