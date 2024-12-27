package io.legere.pdfiumandroid.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.legere.pdfiumandroid.PdfiumSource
import junit.framework.TestCase.fail
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfiumNativeSourceBridgeTest {

    @Test
    fun paramsDispatchedCorrectly() {
        var lastPosition = -1L
        var lastBufferSize = -1
        var lastSize = -1
        val bridge = PdfiumNativeSourceBridge(
            source = mockCustomSource { position, buffer, size ->
                lastPosition = position
                lastBufferSize = buffer.size
                lastSize = size
                size
            }
        )

        try {
            assertEquals(2, bridge.read(1, 2))
            assertEquals(1, lastPosition)
            assertEquals(2, lastBufferSize)
            assertEquals(2, lastSize)
        } catch (t: Exception) {
            fail("Should not throw exception")
        }
    }

    @Test
    fun sizeExceedsIntRange() {
        val bridge = PdfiumNativeSourceBridge(mockCustomSource())

        try {
            assertEquals(0, bridge.read(0, Long.MAX_VALUE))
        } catch (t: Exception) {
            fail("Should not throw exception")
        }
    }

    @Test
    fun sourceThrowsException() {
        val bridge = PdfiumNativeSourceBridge(mockCustomSource { _, _, _ -> error("Exception!") })

        try {
            assertEquals(0, bridge.read(0, 1))
        } catch (t: Exception) {
            fail("Should not throw exception")
        }
    }

    @Test
    fun sourceReturnsNegativeValue() {
        val bridge = PdfiumNativeSourceBridge(mockCustomSource { _, _, _ -> -1 })

        try {
            assertEquals(0, bridge.read(0, 1))
        } catch (t: Exception) {
            fail("Should not throw exception")
        }
    }

    @Test
    fun bufferRescales() {
        var lastBufferSize = -1
        val bridge = PdfiumNativeSourceBridge(
            source = mockCustomSource { _, buffer, _ ->
                lastBufferSize = buffer.size; buffer.size
            }
        )

        try {
            assertEquals(1, bridge.read(0, 1))
            assertEquals(1, lastBufferSize)

            assertEquals(2, bridge.read(0, 2))
            assertEquals(2, lastBufferSize)
        } catch (t: Exception) {
            fail("Should not throw exception")
        }
    }


    // todo: replace with MockK or Mockito when available
    // todo: consider making this a unit test
    private fun mockCustomSource(
        length: Long = 0L,
        read: (Long, ByteArray, Int) -> Int = { _, _, size -> size },
    ): PdfiumSource {
        return object : PdfiumSource {
            override val length: Long
                get() = length

            override fun read(position: Long, buffer: ByteArray, size: Int): Int =
                read(position, buffer, size)
        }
    }

}