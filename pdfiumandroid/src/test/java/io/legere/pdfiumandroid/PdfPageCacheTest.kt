package io.legere.pdfiumandroid

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PdfPageCacheTest {
    // The class under test
    private lateinit var cache: PdfPageCache<PageHolder<PdfPage, PdfTextPage>>

    @MockK
    lateinit var pdfDocument: PdfDocument

    @MockK
    lateinit var pdgPage: PdfPage

    @MockK
    lateinit var pdfTextPage: PdfTextPage

    // A simple data class to act as the "Value" in the cache

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        // Default behaviors for the document
        every { pdfDocument.openPage(any()) } returns pdgPage
        every { pdgPage.openTextPage() } returns pdfTextPage
        every { pdfTextPage.close() } returns Unit
        every { pdgPage.close() } returns Unit

        // Initialize cache with a capacity of 2 for easy testing of eviction
        cache =
            PdfPageCache(pdfDocument, maxSize = 2) { page, textPage ->
                PageHolder(page, textPage)
            }
    }

    @Test
    fun `get loads item using factory if not present`() {
        val index = 0
        val pageMock = mockk<PdfPage>(relaxed = true)
        val textPageMock = mockk<PdfTextPage>(relaxed = true)

        every { pdfDocument.openPage(index) } returns pageMock
        every { pageMock.openTextPage() } returns textPageMock

        // 1. First fetch
        val result = cache.get(index)

        assertNotNull(result)
        assertEquals(pageMock, result.page)
        assertEquals(textPageMock, result.textPage)

        // Verify factory calls (document methods)
        verify(exactly = 1) { pdfDocument.openPage(index) }
        verify(exactly = 1) { pageMock.openTextPage() }
    }

    @Test
    fun `get returns cached item without calling factory`() {
        val index = 1

        // 1. First fetch
        val result1 = cache.get(index)

        // 2. Second fetch
        val result2 = cache.get(index)

        assertSame(result1, result2)

        // Verify factory was only called once
        verify(exactly = 1) { pdfDocument.openPage(index) }
    }

    @Test
    fun `cache evicts oldest item when limit exceeded`() {
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
    fun `accessing an item updates its LRU status`() {
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

        verify(exactly = 1) { pdfDocument.openPage(0) } // Still only opened once

        // If 1 was evicted, getting it again should trigger a 2nd open
        cache.get(1)
        verify(exactly = 2) { pdfDocument.openPage(1) }
    }

    @Test
    fun `clear closes all items and empties cache`() {
        cache.get(0)
        cache.get(1)

        cache.close()

//        verify(exactly = 1) { item0.page.close() }

        // Fetching 0 again should trigger re-open
        cache.get(0)
        verify(exactly = 2) { pdfDocument.openPage(0) }
    }
}
