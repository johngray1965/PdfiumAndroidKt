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

import android.graphics.RectF
import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.legere.pdfiumandroid.core.jni.NativePageLinkContract
import io.legere.pdfiumandroid.core.jni.defaultNativeFactory
import java.io.Closeable
import java.nio.charset.StandardCharsets

@Suppress("TooManyFunctions")
class PdfPageLinkU(
    private val pageLinkPtr: Long,
    nativeFactory: NativeFactory = defaultNativeFactory,
) : Closeable {
    private val nativePageLink: NativePageLinkContract = nativeFactory.getNativePageLink()

    fun countWebLinks(): Int = nativePageLink.countWebLinks(pageLinkPtr)

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    fun getURL(
        index: Int,
        length: Int,
    ): String? {
        try {
            val bytes = ByteArray(length * 2)
            val r =
                nativePageLink.getURL(
                    pageLinkPtr,
                    index,
                    length,
                    bytes,
                )

            if (r <= 0) {
                return ""
            }
            return String(bytes, StandardCharsets.UTF_16LE)
        } catch (e: NullPointerException) {
            Logger.e(TAG, e, "mContext may be null")
        } catch (e: Exception) {
            Logger.e(TAG, e, "Exception throw from native")
        }
        return null
    }

    fun countRects(index: Int): Int = nativePageLink.countRects(pageLinkPtr, index)

    @Suppress("MagicNumber")
    fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF =
        nativePageLink.getRect(pageLinkPtr, linkIndex, rectIndex).let {
            RectF(it[0], it[1], it[2], it[3])
        }

    fun getTextRange(index: Int): Pair<Int, Int> =
        nativePageLink.getTextRange(pageLinkPtr, index).let {
            Pair(it[0], it[1])
        }

    override fun close() {
        nativePageLink.closePageLink(pageLinkPtr)
    }

    /**
     * @suppress
     */
    companion object {
        private val TAG = PdfPageLinkU::class.java.name
    }
}
