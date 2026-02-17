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

import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.legere.pdfiumandroid.core.jni.NativeFindResultContract
import io.legere.pdfiumandroid.core.jni.defaultNativeFactory
import java.io.Closeable

@Suppress("TooManyFunctions")
class FindResultU(
    val handle: FindHandle,
    nativeFactory: NativeFactory = defaultNativeFactory,
) : Closeable {
    private val nativeFindResult: NativeFindResultContract = nativeFactory.getNativeFindResult()

    fun findNext(): Boolean = nativeFindResult.findNext(handle)

    fun findPrev(): Boolean = nativeFindResult.findPrev(handle)

    fun getSchResultIndex(): Int = nativeFindResult.getSchResultIndex(handle)

    fun getSchCount(): Int = nativeFindResult.getSchCount(handle)

    fun closeFind() {
        nativeFindResult.closeFind(handle)
    }

    override fun close() {
        nativeFindResult.closeFind(handle)
    }
}
