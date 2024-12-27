package io.legere.pdfiumandroid.util

import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.PdfiumSource
import junit.framework.TestCase.fail
import org.junit.Test

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
            assertThat(lastPosition).isEqualTo(1)
            assertThat(lastBufferSize).isEqualTo(2)
            assertThat(lastSize).isEqualTo(2)
        } catch (t: Exception) {
            fail("Should not throw exception")
        }
    }

    @Test
    @Suppress("SwallowedException")
    fun sizeExceedsIntRange() {
        val bridge = PdfiumNativeSourceBridge(mockCustomSource())

        try {
            assertThat(bridge.read(0, Long.MAX_VALUE)).isEqualTo(0)
        } catch (t: Exception) {
            fail("Should not throw exception")
        }
    }

    @Test
    @Suppress("SwallowedException")
    fun sourceThrowsException() {
        val bridge = PdfiumNativeSourceBridge(mockCustomSource { _, _, _ -> error("Exception!") })

        try {
            assertThat(bridge.read(0, 1)).isEqualTo(0)
        } catch (t: Exception) {
            fail("Should not throw exception")
        }
    }

    @Test
    @Suppress("SwallowedException")
    fun sourceReturnsNegativeValue() {
        val bridge = PdfiumNativeSourceBridge(mockCustomSource { _, _, _ -> -1 })

        try {
            assertThat(bridge.read(0, 1)).isEqualTo(0)
        } catch (t: Exception) {
            fail("Should not throw exception")
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
            assertThat(lastBufferSize).isEqualTo(1)

            assertThat(bridge.read(0, 2)).isEqualTo(2)
            assertThat(lastBufferSize).isEqualTo(2)
        } catch (t: Exception) {
            fail("Should not throw exception")
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
        }
}
