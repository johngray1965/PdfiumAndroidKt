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

package io.legere.pdfiumandroid.api

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.core.util.PdfiumNativeSourceBridge
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PdfiumNativeSourceBridgeTest {
    @Test
    @Suppress("SwallowedException")
    fun paramsDispatchedCorrectly() {
        var lastPosition = -1L
        var lastBufferSize = -1
        var lastSize = -1
        val bridge =
            PdfiumNativeSourceBridge(
                source =
                    mockCustomSource { position, buffer, size ->
                        lastPosition = position
                        lastBufferSize = buffer.size
                        lastSize = size
                        size
                    },
            )

        try {
            assertThat(bridge.read(1, 2)).isEqualTo(2)
            Truth.assertThat(lastPosition).isEqualTo(1)
            Truth.assertThat(lastBufferSize).isEqualTo(2)
            Truth.assertThat(lastSize).isEqualTo(2)
        } catch (_: Exception) {
            Assertions.fail("Should not throw exception")
        }
    }

    @Test
    @Suppress("SwallowedException")
    fun sizeExceedsIntRange() {
        val bridge = PdfiumNativeSourceBridge(mockCustomSource())

        try {
            assertThat(bridge.read(0, Long.MAX_VALUE)).isEqualTo(0)
        } catch (_: Exception) {
            Assertions.fail("Should not throw exception")
        }
    }

    @Test
    @Suppress("SwallowedException")
    fun sourceThrowsException() {
        val bridge = PdfiumNativeSourceBridge(mockCustomSource { _, _, _ -> error("Exception!") })

        try {
            assertThat(bridge.read(0, 1)).isEqualTo(0)
        } catch (_: Exception) {
            Assertions.fail("Should not throw exception")
        }
    }

    @Test
    @Suppress("SwallowedException")
    fun sourceReturnsNegativeValue() {
        val bridge = PdfiumNativeSourceBridge(mockCustomSource { _, _, _ -> -1 })

        try {
            assertThat(bridge.read(0, 1)).isEqualTo(0)
        } catch (_: Exception) {
            Assertions.fail("Should not throw exception")
        }
    }

    @Test
    @Suppress("SwallowedException")
    fun bufferRescales() {
        var lastBufferSize = -1
        val bridge =
            PdfiumNativeSourceBridge(
                source =
                    mockCustomSource { _, buffer, _ ->
                        lastBufferSize = buffer.size
                        buffer.size
                    },
            )

        try {
            assertThat(bridge.read(0, 1)).isEqualTo(1)
            Truth.assertThat(lastBufferSize).isEqualTo(1)

            assertThat(bridge.read(0, 2)).isEqualTo(2)
            Truth.assertThat(lastBufferSize).isEqualTo(2)
        } catch (_: Exception) {
            Assertions.fail("Should not throw exception")
        }
    }

    // todo: replace with MockK or Mockito when available
    private fun mockCustomSource(
        length: Long = 0L,
        read: (Long, ByteArray, Int) -> Int = { _, _, size -> size },
    ): PdfiumSource =
        object : PdfiumSource {
            override val length: Long
                get() = length

            override fun read(
                position: Long,
                buffer: ByteArray,
                size: Int,
            ): Int = read(position, buffer, size)

            override fun close() {
                // nothing to close
            }
        }
}
