package io.legere.pdfiumandroid

import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.unlocked.FindResultU
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FindResultTest {
    lateinit var findResult: FindResult

    @MockK lateinit var findResultU: FindResultU

    @BeforeEach
    fun setUp() {
        findResult = FindResult(findResultU)
    }

    @Test
    fun `findNext successful`() {
        every { findResultU.findNext() } returns true
        val result = findResult.findNext()
        assertThat(result).isTrue()
    }

    @Test
    fun `findNext fails`() {
        every { findResultU.findNext() } returns false
        val result = findResult.findNext()
        assertThat(result).isFalse()
    }

    @Test
    fun `findPrev successful`() {
        every { findResultU.findPrev() } returns true
        val result = findResult.findPrev()
        assertThat(result).isTrue()
    }

    @Test
    fun `findPrev fails`() {
        every { findResultU.findPrev() } returns false
        val result = findResult.findPrev()
        assertThat(result).isFalse()
    }

    @Test
    fun getSchResultIndex() {
        every { findResultU.getSchResultIndex() } returns 23
        val result = findResult.getSchResultIndex()
        assertThat(result).isEqualTo(23)
        verify { findResultU.getSchResultIndex() }
    }

    @Test
    fun getSchCount() {
        every { findResultU.getSchCount() } returns 47
        val result = findResult.getSchCount()
        assertThat(result).isEqualTo(47)
        verify { findResultU.getSchCount() }
    }

    @Test
    fun closeFind() {
        every { findResultU.closeFind() } returns Unit
        findResult.closeFind()
        verify { findResultU.closeFind() }
    }

    @Test
    fun close() {
        every { findResultU.close() } returns Unit
        findResult.close()
        verify { findResultU.close() }
    }
}
