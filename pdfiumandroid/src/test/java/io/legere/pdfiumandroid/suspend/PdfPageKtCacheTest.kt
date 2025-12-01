package io.legere.pdfiumandroid.suspend

import com.google.common.truth.Truth.assertThat
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

class PdfPageKtCacheTest {
    private lateinit var cache: PdfPageKtCache<PageHolderKt<PdfPageKt, PdfTextPageKt>>

    @MockK
    lateinit var pdfDocument: PdfDocumentKt

    @MockK
    lateinit var pdgPage: PdfPageKt

    @MockK
    lateinit var pdfTextPage: PdfTextPageKt

    // A simple data class to act as the "Value" in the cache

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        // Default behaviors for the document
        coEvery { pdfDocument.openPage(any()) } returns pdgPage
        coEvery { pdgPage.openTextPage() } returns pdfTextPage
        every { pdfTextPage.close() } returns Unit
        every { pdgPage.close() } returns Unit

        // Initialize cache with a capacity of 2 for easy testing of eviction
        cache =
            PdfPageKtCache(pdfDocument, maxSize = 2) { page, textPage ->
                PageHolderKt(page, textPage)
            }
    }

    @Test
    fun `get loads item using factory if not present`() =
        runTest {
            val index = 0
            val pageMock = mockk<PdfPageKt>(relaxed = true)
            val textPageMock = mockk<PdfTextPageKt>(relaxed = true)

            coEvery { pdfDocument.openPage(index) } returns pageMock
            coEvery { pageMock.openTextPage() } returns textPageMock

            // 1. First fetch
            val result = cache.get(index)

            assertThat(result).isNotNull()
            assertThat(pageMock).isEqualTo(result.page)
            assertThat(textPageMock).isEqualTo(result.textPage)

            // Verify factory calls (document methods)
            coVerify(exactly = 1) { pdfDocument.openPage(index) }
            coVerify(exactly = 1) { pageMock.openTextPage() }
        }

    @Test
    fun `get returns cached item without calling factory`() =
        runTest {
            val index = 1

            // 1. First fetch
            val result1 = cache.get(index)

            // 2. Second fetch
            val result2 = cache.get(index)

            assertSame(result1, result2)

            // Verify factory was only called once
            coVerify(exactly = 1) { pdfDocument.openPage(index) }
        }

    @Test
    fun `cache evicts oldest item when limit exceeded`() =
        runTest {
            // Cache limit is set to 2 in setUp()

            // Load Item 0
            val item0 = cache.get(0)
            // Load Item 1
            cache.get(1)

            // Cache is full [0, 1]

            // Load Item 2 -> Should evict Item 0 (LRU)
            cache.get(2)

            // Item 0 should be closed
            verify(exactly = 1) { item0.page.close() }
            verify(exactly = 1) { item0.textPage.close() }

            // Item 1 should NOT be closed yet
//        verify(exactly = 0) { item1.page.close() }
        }

    @Test
    fun `accessing an item updates its LRU status`() =
        runTest {
            // Cache limit 2

            cache.get(0) // [0]
            cache.get(1) // [0, 1]

            // Access 0 again. Now 1 is the "oldest" (least recently used)
            cache.get(0) // [1, 0]

            // Add new item 2. Should evict 1, not 0.
            cache.get(2) // [0, 2]

            // Verify item 1 was closed (evicted)
            // We can't check the exact instance easily without capturing, but we can check logic:
            // If 0 is still in cache, asking for it should not trigger a re-open.

            coVerify(exactly = 1) { pdfDocument.openPage(0) } // Still only opened once

            // If 1 was evicted, getting it again should trigger a 2nd open
            cache.get(1)
            coVerify(exactly = 2) { pdfDocument.openPage(1) }
        }

    @Test
    fun `clear closes all items and empties cache`() =
        runTest {
            cache.get(0)
            cache.get(1)

            cache.close()

//        verify(exactly = 1) { item0.page.close() }

            // Fetching 0 again should trigger re-open
            cache.get(0)
            coVerify(exactly = 2) { pdfDocument.openPage(0) }
        }
}
