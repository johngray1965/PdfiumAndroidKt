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
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.base.ByteArrayPdfiumSource
import io.legere.pdfiumandroid.core.util.PdfiumNativeSourceBridge
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeCoreTest : BasePDFTest() {
    val nativeCore = NativeCore()

    @Before
    fun setUp() {
        System.loadLibrary("pdfium")
        System.loadLibrary("pdfiumandroid")
    }

    @Test
    fun openDocument() {
        getPdfPath("f01.pdf")?.use {
            val docPtr = nativeCore.openDocument(it.fd, null)
            assertThat(docPtr).isNotNull()
        }
    }

    @Test
    fun openMemDocument() {
        val pdfBytes = getPdfBytes("f01.pdf")
        val docPtr = nativeCore.openMemDocument(pdfBytes, null)
        assertThat(docPtr).isNotNull()
    }

    @Test
    fun openCustomDocument() {
        val pdfBytes = getPdfBytes("f01.pdf")

        val data = ByteArrayPdfiumSource(pdfBytes!!)

        val nativeSourceBridge = PdfiumNativeSourceBridge(data)
        val docPtr = nativeCore.openCustomDocument(nativeSourceBridge, null, pdfBytes.size.toLong())
        assertThat(docPtr).isNotNull()
    }
}
