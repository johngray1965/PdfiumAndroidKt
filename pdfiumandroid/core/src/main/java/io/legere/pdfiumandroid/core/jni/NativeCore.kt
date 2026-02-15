package io.legere.pdfiumandroid.core.jni

import io.legere.pdfiumandroid.core.util.PdfiumNativeSourceBridge

/**
 * Contract for the native core PDFium functions.
 * This interface defines the JNI methods for opening PDF documents.
 * Implementations of this contract are intended for **internal use only**
 * within the PdfiumAndroid library to abstract native calls.
 */
interface NativeCoreContract {
    /**
     * Opens a PDF document from a file descriptor.
     * This is a JNI method.
     *
     * @param fd The file descriptor of the PDF file.
     * @param password The password for the PDF document, or `null` if no password is required.
     * @return A native pointer (long) to the opened PDF document.
     */
    fun openDocument(
        fd: Int,
        password: String?,
    ): Long

    /**
     * Opens a PDF document from a byte array in memory.
     * This is a JNI method.
     *
     * @param data The byte array containing the PDF document data.
     * @param password The password for the PDF document, or `null` if no password is required.
     * @return A native pointer (long) to the opened PDF document.
     */
    fun openMemDocument(
        data: ByteArray?,
        password: String?,
    ): Long

    /**
     * Opens a PDF document from a custom data source, bridged via
     * [PdfiumNativeSourceBridge].
     * This is a JNI method.
     *
     * @param data The [PdfiumNativeSourceBridge] instance to read PDF data.
     * @param password The password for the PDF document, or `null` if no password is required.
     * @param size The total length of the PDF data available from the source.
     * @return A native pointer (long) to the opened PDF document.
     */
    fun openCustomDocument(
        data: PdfiumNativeSourceBridge,
        password: String?,
        size: Long,
    ): Long
}

class NativeCore : NativeCoreContract {
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

    override fun openDocument(
        fd: Int,
        password: String?,
    ): Long =
        nativeOpenDocument(
            fd,
            password,
        )

    override fun openMemDocument(
        data: ByteArray?,
        password: String?,
    ): Long =
        nativeOpenMemDocument(
            data,
            password,
        )

    override fun openCustomDocument(
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
