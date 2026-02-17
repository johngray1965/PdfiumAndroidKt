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

import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.arrow.testing.StandardTestDispatcherExtension
import io.legere.pdfiumandroid.core.unlocked.FindResultU
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
class FindResultKtFTest {
    private lateinit var findResult: FindResultKtF

    @MockK
    lateinit var findResultU: FindResultU

    @BeforeEach
    fun setUp() {
        findResult = FindResultKtF(findResultU, Dispatchers.Unconfined)
    }

    @Test
    fun findNext() =
        runTest {
            every { findResultU.findNext() } returns true
            val result = findResult.findNext().getOrNull()
            assertThat(result).isTrue()
            verify { findResultU.findNext() }
        }

    @Test
    fun findPrev() =
        runTest {
            every { findResultU.findPrev() } returns true
            val result = findResult.findPrev().getOrNull()
            assertThat(result).isTrue()
            verify { findResultU.findPrev() }
        }

    @Test
    fun getSchResultIndex() =
        runTest {
            val expected = 14
            every { findResultU.getSchResultIndex() } returns expected
            val result = findResult.getSchResultIndex().getOrNull()
            assertThat(result).isEqualTo(expected)
            verify { findResultU.getSchResultIndex() }
        }

    @Test
    fun getSchCount() =
        runTest {
            val expected = 14
            every { findResultU.getSchCount() } returns expected
            val result = findResult.getSchCount().getOrNull()
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
