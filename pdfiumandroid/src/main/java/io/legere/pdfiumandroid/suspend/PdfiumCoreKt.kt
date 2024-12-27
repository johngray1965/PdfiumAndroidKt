@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.os.ParcelFileDescriptor
import androidx.annotation.Keep
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.PdfiumSource
import io.legere.pdfiumandroid.util.Config
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * PdfiumCoreKt is the main entry-point for access to the PDFium API.
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 * @constructor create a [PdfiumCoreKt] from a [PdfiumCore]
 */
@Keep
class PdfiumCoreKt(
    private val dispatcher: CoroutineDispatcher,
    config: Config = Config(),
) {
    private val coreInternal = PdfiumCore(config = config)

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(fd: ParcelFileDescriptor): PdfDocumentKt =
        withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(fd), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        fd: ParcelFileDescriptor,
        password: String?,
    ): PdfDocumentKt =
        withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(fd, password), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: ByteArray?): PdfDocumentKt =
        withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        data: ByteArray?,
        password: String?,
    ): PdfDocumentKt =
        withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data, password), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: PdfiumSource): PdfDocumentKt =
        withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        data: PdfiumSource,
        password: String?,
    ): PdfDocumentKt =
        withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data, password), dispatcher)
        }
}
