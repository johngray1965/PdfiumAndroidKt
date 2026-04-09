/*
 * Copyright 2023-2026 John Gray
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
import io.legere.pdfiumandroid.api.PdfPasswordException
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativePasswordTest : BasePDFTest() {

    @Test
    fun openPasswordProtectedPdfWithoutPasswordThrowsPdfPasswordException() {
        val pdfBytes = getPdfBytes("test_password.pdf")
        assertThat(pdfBytes).isNotNull()

        assertThrows(PdfPasswordException::class.java) {
            PdfiumCoreU().newDocument(pdfBytes)
        }
    }

    @Test
    fun openPasswordProtectedPdfWithCorrectPasswordSucceeds() {
        val pdfBytes = getPdfBytes("test_password.pdf")
        assertThat(pdfBytes).isNotNull()

        val document = PdfiumCoreU().newDocument(pdfBytes, "testpass")
        assertThat(document).isNotNull()
        document.close()
    }

    @Test
    fun openPasswordProtectedPdfWithWrongPasswordThrowsPdfPasswordException() {
        val pdfBytes = getPdfBytes("test_password.pdf")
        assertThat(pdfBytes).isNotNull()

        assertThrows(PdfPasswordException::class.java) {
            PdfiumCoreU().newDocument(pdfBytes, "wrongpassword")
        }
    }
}
