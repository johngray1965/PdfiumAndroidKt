package io.legere.pdfiumandroid.core.unlocked

import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.legere.pdfiumandroid.core.jni.NativeFindResult
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FindResultUTest {
    lateinit var findResult: FindResultU

    @MockK lateinit var mockNativeFactory: NativeFactory

    @MockK lateinit var nativeFindResult: NativeFindResult

    @BeforeEach
    fun setup() {
        PdfiumCoreU.resetForTesting()
        every { mockNativeFactory.getNativeFindResult() } returns nativeFindResult
        findResult = FindResultU(124L, mockNativeFactory)
    }

    @Test
    fun testFindNextIsTrue() {
        every { nativeFindResult.findNext(any()) } returns true
        assertThat(findResult.findNext()).isTrue()
    }

    @Test
    fun testFindNextIsFalse() {
        every { nativeFindResult.findNext(any()) } returns false
        assertThat(findResult.findNext()).isFalse()
    }

    @Test
    fun testFindPrevIsTrue() {
        every { nativeFindResult.findPrev(any()) } returns true
        assertThat(findResult.findPrev()).isTrue()
    }

    @Test
    fun testFindPrevIsFalse() {
        every { nativeFindResult.findPrev(any()) } returns false
        assertThat(findResult.findPrev()).isFalse()
    }

    @Test
    fun testGetSchCount() {
        every { nativeFindResult.getSchCount(any()) } returns 123
        assertThat(findResult.getSchCount()).isEqualTo(123)
    }

    @Test
    fun testGetSchResultIndex() {
        every { nativeFindResult.getSchResultIndex(any()) } returns 123
        assertThat(findResult.getSchResultIndex()).isEqualTo(123)
    }

    @Test
    fun testCloseFind() {
        every { nativeFindResult.closeFind(any()) } just runs
        findResult.closeFind()
        verify { nativeFindResult.closeFind(any()) }
    }

    @Test
    fun testClose() {
        every { nativeFindResult.closeFind(any()) } just runs
        findResult.close()
        verify { nativeFindResult.closeFind(any()) }
    }
}
