/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023-2026 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.legere.pdfiumandroid.core.jni

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU
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
        assertThat(index).isEqualTo(1525)
        hasNext = nativeFindResult.findNext(findResult)
        assertThat(hasNext).isTrue()
        index = nativeFindResult.getSchResultIndex(findResult)
        assertThat(index).isEqualTo(2761)
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
        assertThat(index).isEqualTo(2761)
        hasPrev = nativeFindResult.findPrev(findResult)
        assertThat(hasPrev).isTrue()
        index = nativeFindResult.getSchResultIndex(findResult)
        assertThat(index).isEqualTo(1525)
        hasPrev = nativeFindResult.findPrev(findResult)
        assertThat(hasPrev).isFalse()

        nativeFindResult.closeFind(findResult)
    }
}
