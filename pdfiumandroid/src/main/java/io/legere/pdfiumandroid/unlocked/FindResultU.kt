package io.legere.pdfiumandroid.unlocked

import io.legere.pdfiumandroid.FindHandle
import io.legere.pdfiumandroid.jni.NativeFactory
import io.legere.pdfiumandroid.jni.NativeFindResultContract
import io.legere.pdfiumandroid.jni.defaultNativeFactory
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
