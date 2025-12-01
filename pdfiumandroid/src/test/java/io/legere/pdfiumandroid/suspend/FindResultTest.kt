package io.legere.pdfiumandroid.suspend

import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.testing.StandardTestDispatcherExtension
import io.legere.pdfiumandroid.unlocked.FindResultU
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, StandardTestDispatcherExtension::class)
class FindResultTest {
    private lateinit var findResult: FindResultKt

    @MockK
    lateinit var findResultU: FindResultU

    @BeforeEach
    fun setUp() {
        findResult = FindResultKt(findResultU, Dispatchers.Unconfined)
    }

    @Test
    fun findNext() =
        runTest {
            every { findResultU.findNext() } returns true
            val result = findResult.findNext()
            assertThat(result).isTrue()
            verify { findResultU.findNext() }
        }

    @Test
    fun findPrev() =
        runTest {
            every { findResultU.findPrev() } returns true
            val result = findResult.findPrev()
            assertThat(result).isTrue()
            verify { findResultU.findPrev() }
        }

    @Test
    fun getSchResultIndex() =
        runTest {
            val expected = 14
            every { findResultU.getSchResultIndex() } returns expected
            val result = findResult.getSchResultIndex()
            assertThat(result).isEqualTo(expected)
            verify { findResultU.getSchResultIndex() }
        }

    @Test
    fun getSchCount() =
        runTest {
            val expected = 14
            every { findResultU.getSchCount() } returns expected
            val result = findResult.getSchCount()
            assertThat(result).isEqualTo(expected)
            verify { findResultU.getSchCount() }
        }

    @Test
    fun closeFind() =
        runTest {
            every { findResultU.closeFind() } returns Unit
            findResult.closeFind()
            verify { findResultU.closeFind() }
        }

    @Test
    fun close() {
        every { findResultU.closeFind() } returns Unit
        findResult.close()
        verify { findResultU.closeFind() }
    }
}
