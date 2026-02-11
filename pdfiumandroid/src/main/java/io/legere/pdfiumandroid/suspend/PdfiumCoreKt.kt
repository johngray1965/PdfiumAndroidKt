@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.os.ParcelFileDescriptor
import androidx.annotation.Keep
import io.legere.pdfiumandroid.LockManager
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.PdfiumSource
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
import io.legere.pdfiumandroid.util.Config
import io.legere.pdfiumandroid.util.pdfiumConfig
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
    private val coreInternal: PdfiumCoreU = PdfiumCoreU(config = config),
) {
    private val lock: LockManager = pdfiumConfig.lock

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(parcelFileDescriptor: ParcelFileDescriptor): PdfDocumentKt =
        lock.withLock {
            withContext(dispatcher) {
                PdfDocumentKt(coreInternal.newDocument(parcelFileDescriptor), dispatcher)
            }
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        parcelFileDescriptor: ParcelFileDescriptor,
        password: String?,
    ): PdfDocumentKt =
        lock.withLock {
            withContext(dispatcher) {
                PdfDocumentKt(coreInternal.newDocument(parcelFileDescriptor, password), dispatcher)
            }
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: ByteArray?): PdfDocumentKt =
        lock.withLock {
            withContext(dispatcher) {
                PdfDocumentKt(coreInternal.newDocument(data), dispatcher)
            }
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        data: ByteArray?,
        password: String?,
    ): PdfDocumentKt =
        lock.withLock {
            withContext(dispatcher) {
                PdfDocumentKt(coreInternal.newDocument(data, password), dispatcher)
            }
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: PdfiumSource): PdfDocumentKt =
        lock.withLock {
            withContext(dispatcher) {
                PdfDocumentKt(coreInternal.newDocument(data), dispatcher)
            }
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        data: PdfiumSource,
        password: String?,
    ): PdfDocumentKt =
        lock.withLock {
            withContext(dispatcher) {
                PdfDocumentKt(coreInternal.newDocument(data, password), dispatcher)
            }
        }
}
