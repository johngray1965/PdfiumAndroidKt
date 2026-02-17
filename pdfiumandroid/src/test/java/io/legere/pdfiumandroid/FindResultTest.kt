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

package io.legere.pdfiumandroid

import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.core.unlocked.FindResultU
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
