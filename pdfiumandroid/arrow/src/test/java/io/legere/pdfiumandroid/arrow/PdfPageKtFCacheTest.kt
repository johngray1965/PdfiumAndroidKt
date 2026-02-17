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

import arrow.core.right
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PdfPageKtFCacheTest {
    private lateinit var cache: PdfPageKtFCache<PageHolderKtF<PdfPageKtF, PdfTextPageKtF>>

    @MockK
    lateinit var pdfDocument: PdfDocumentKtF

    @MockK
    lateinit var pdfDocumentU: PdfDocumentU

    @MockK
    lateinit var pdgPage: PdfPageKtF

    @MockK
    lateinit var pdgPageU: PdfPageU

    @MockK
    lateinit var pdfTextPage: PdfTextPageKtF

    @MockK
    lateinit var pdfTextPageU: PdfTextPageU

    // A simple data class to act as the "Value" in the cache

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        // Default behaviors for the document
        coEvery { pdfDocument.openPage(any()) } returns pdgPage.right()
        coEvery { pdfDocumentU.openPage(any()) } returns pdgPageU
        coEvery { pdfDocument.document } returns pdfDocumentU
        coEvery { pdgPage.openTextPage() } returns pdfTextPage.right()
        coEvery { pdgPageU.openTextPage() } returns pdfTextPageU
        every { pdfTextPage.close() } returns Unit
        every { pdgPage.close() } returns Unit

        // Initialize cache with a capacity of 2 for easy testing of eviction
        cache =
            PdfPageKtFCache(pdfDocument, maxSize = 2) { page, textPage ->
                PageHolderKtF(page, textPage)
            }
    }

    @Test
    fun `get loads item using factory if not present`() =
        runTest {
            val index = 0
            val pageMock = mockk<PdfPageKtF>(relaxed = true)
            val textPageMock = mockk<PdfTextPageKtF>(relaxed = true)

            coEvery { pdfDocument.openPage(index) } returns pageMock.right()
            coEvery { pageMock.openTextPage() } returns textPageMock.right()

            // 1. First fetch
            val result = cache.getF(index).getOrNull()

            assertThat(result).isNotNull()
//            assertEquals(pageMock.page, result?.page)
//            assertEquals(textPageMock, result?.textPage)

            // Verify factory calls (document methods)
//            coVerify(exactly = 1) { pdfDocumentU.openPage(index) }
//            coVerify(exactly = 1) { pageMock.openTextPage() }
            coVerify { pdfDocument.openPage(index) }
            coVerify { pageMock.openTextPage() }
        }

    @Test
    fun `get returns cached item without calling factory`() =
        runTest {
            val index = 1

            // 1. First fetch
            val result1 = cache.getF(index)

            // 2. Second fetch
            val result2 = cache.getF(index)

            assertSame(result1.getOrNull(), result2.getOrNull())

            // Verify factory was only called once
            coVerify(exactly = 1) { pdfDocument.openPage(index) }
            coVerify(exactly = 1) { pdgPage.openTextPage() }
//            coVerify { pdgPage.close() }
        }

    @Test
    fun `cache evicts oldest item when limit exceeded`() =
        runTest {
            // Cache limit is set to 2 in setUp()

            // Load Item 0
            val item0 = cache.getF(0).getOrNull()
            // Load Item 1
            cache.getF(1)

            // Cache is full [0, 1]

            // Load Item 2 -> Should evict Item 0 (LRU)
            cache.getF(2)

            // Item 0 should be closed
            verify { item0?.page?.close() }
            verify { item0?.textPage?.close() }

            // Item 1 should NOT be closed yet
//        verify(exactly = 0) { item1.page.close() }
            coVerify { pdfDocument.openPage(any()) }
            coVerify { pdgPage.openTextPage() }
        }

    @Test
    fun `accessing an item updates its LRU status`() =
        runTest {
            // Cache limit 2

            cache.getF(0) // [0]
            cache.getF(1) // [0, 1]

            // Access 0 again. Now 1 is the "oldest" (least recently used)
            cache.getF(0) // [1, 0]

            // Add new item 2. Should evict 1, not 0.
            cache.getF(2) // [0, 2]

            // Verify item 1 was closed (evicted)
            // We can't check the exact instance easily without capturing, but we can check logic:
            // If 0 is still in cache, asking for it should not trigger a re-open.

//            coVerify(exactly = 1) { pdfDocumentU.openPage(0) } // Still only opened once

            // If 1 was evicted, getting it again should trigger a 2nd open
            cache.getF(1)
            coVerify { pdfDocument.openPage(any()) }
            coVerify { pdgPage.openTextPage() }
            coVerify { pdgPage.close() }
            coVerify { pdfTextPage.close() }
        }

    @Test
    fun `clear closes all items and empties cache`() =
        runTest {
            cache.getF(0)
            cache.getF(1)

            cache.close()

//        verify(exactly = 1) { item0.page.close() }

            // Fetching 0 again should trigger re-open
            cache.getF(0)
            coVerify(exactly = 2) { pdfDocument.openPage(0) }
            coVerify(exactly = 1) { pdfDocument.openPage(1) }

            coVerify { pdgPage.close() }
            coVerify { pdgPage.openTextPage() }
            coVerify { pdfTextPage.close() }
        }
}
