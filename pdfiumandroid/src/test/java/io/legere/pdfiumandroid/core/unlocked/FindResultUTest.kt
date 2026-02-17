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
