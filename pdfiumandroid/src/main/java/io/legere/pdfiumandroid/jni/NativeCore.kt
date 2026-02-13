package io.legere.pdfiumandroid.jni

import io.legere.pdfiumandroid.util.PdfiumNativeSourceBridge

class NativeCore {
    private external fun nativeOpenDocument(
        fd: Int,
        password: String?,
    ): Long

    private external fun nativeOpenMemDocument(
        data: ByteArray?,
        password: String?,
    ): Long

    private external fun nativeOpenCustomDocument(
        data: PdfiumNativeSourceBridge,
        password: String?,
        size: Long,
    ): Long

    internal fun openDocument(
        fd: Int,
        password: String?,
    ): Long =
        nativeOpenDocument(
            fd,
            password,
        )

    internal fun openMemDocument(
        data: ByteArray?,
        password: String?,
    ): Long =
        nativeOpenMemDocument(
            data,
            password,
        )

    internal fun openCustomDocument(
        data: PdfiumNativeSourceBridge,
        password: String?,
        size: Long,
    ): Long =
        nativeOpenCustomDocument(
            data,
            password,
            size,
        )
}
