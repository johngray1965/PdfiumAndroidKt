@file:Suppress("unused")

package io.legere.pdfiumandroid.arrow

import android.os.ParcelFileDescriptor
import arrow.core.Either
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.PdfiumSource
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
import io.legere.pdfiumandroid.util.Config
import kotlinx.coroutines.CoroutineDispatcher

/**
 * PdfiumCoreKtF is the main entry-point for access to the PDFium API.
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 * @constructor create a [PdfiumCoreKtF] from a [PdfiumCore]
 */
class PdfiumCoreKtF(
    private val dispatcher: CoroutineDispatcher,
    config: Config = Config(),
    private val coreInternal: PdfiumCoreU = PdfiumCoreU(config = config),
) {
    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(fd: ParcelFileDescriptor): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(fd), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        fd: ParcelFileDescriptor,
        password: String?,
    ): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(fd, password), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: ByteArray?): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(data), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        data: ByteArray?,
        password: String?,
    ): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(data, password), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: PdfiumSource): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(data), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        data: PdfiumSource,
        password: String?,
    ): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(data, password), dispatcher)
        }
}
