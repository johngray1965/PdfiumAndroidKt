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

package io.legere.pdfiumandroid.arrow

import android.os.ParcelFileDescriptor
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.LockManager
import io.legere.pdfiumandroid.api.LockManagerReentrantLock
import io.legere.pdfiumandroid.api.PdfiumSource
import io.legere.pdfiumandroid.arrow.testing.StandardTestDispatcherExtension
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, StandardTestDispatcherExtension::class)
class PdfiumCoreKtFTest {
    private lateinit var core: PdfiumCoreKtF

    @MockK
    private lateinit var coreInternal: PdfiumCoreU

    @MockK
    private lateinit var document: PdfDocumentU

    @BeforeEach
    fun setUp() {
        core = PdfiumCoreKtF(Dispatchers.Main, coreInternal = coreInternal)
        core.setLockManager(LockManagerReentrantLock())
    }

    @Test
    fun newDocument() =
        runTest {
            val parcelFileDescriptor = mockk<ParcelFileDescriptor>()
            coEvery { coreInternal.newDocument(any<ParcelFileDescriptor>()) } returns document
            val result = core.newDocument(parcelFileDescriptor).getOrNull()
            assertThat(result?.document).isEqualTo(document)
            coVerify { coreInternal.newDocument(any<ParcelFileDescriptor>()) }
        }

    @Test
    fun testNewDocument() =
        runTest {
            val parcelFileDescriptor = mockk<ParcelFileDescriptor>()
            coEvery { coreInternal.newDocument(any<ParcelFileDescriptor>(), any()) } returns document
            val result = core.newDocument(parcelFileDescriptor, "password").getOrNull()
            assertThat(result?.document).isEqualTo(document)
            coVerify { coreInternal.newDocument(any<ParcelFileDescriptor>(), any()) }
        }

    @Test
    fun testNewDocument1() =
        runTest {
            val data = byteArrayOf(0x25, 0x50, 0x44, 0x46)
            coEvery { coreInternal.newDocument(any<ByteArray>()) } returns document
            val result = core.newDocument(data).getOrNull()
            assertThat(result?.document).isEqualTo(document)
            coVerify { coreInternal.newDocument(any<ByteArray>()) }
        }

    @Test
    fun testNewDocument2() =
        runTest {
            val data = byteArrayOf(0x25, 0x50, 0x44, 0x46)
            coEvery { coreInternal.newDocument(any<ByteArray>(), any()) } returns document
            val result = core.newDocument(data, "password").getOrNull()
            assertThat(result?.document).isEqualTo(document)
            coVerify { coreInternal.newDocument(any<ByteArray>(), any()) }
        }

    @Test
    fun testNewDocument3() =
        runTest {
            val pdfiumSource = mockk<PdfiumSource>()
            coEvery { coreInternal.newDocument(any<PdfiumSource>()) } returns document
            val result = core.newDocument(pdfiumSource).getOrNull()
            assertThat(result?.document).isEqualTo(document)
            coVerify { coreInternal.newDocument(any<PdfiumSource>()) }
        }

    @Test
    fun testNewDocument4() =
        runTest {
            val pdfiumSource = mockk<PdfiumSource>()
            coEvery { coreInternal.newDocument(any<PdfiumSource>(), any()) } returns document
            val result = core.newDocument(pdfiumSource, "password").getOrNull()
            assertThat(result?.document).isEqualTo(document)
            coVerify { coreInternal.newDocument(any<PdfiumSource>(), any()) }
        }

    @Test
    fun setLockManager() =
        runTest {
            val expected = mockk<LockManager>()
            core.setLockManager(expected)

            assertThat(PdfiumCoreU.lock).isEqualTo(expected)
        }
}
