@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.os.ParcelFileDescriptor
import androidx.annotation.Keep
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.api.Config
import io.legere.pdfiumandroid.api.LockManager
import io.legere.pdfiumandroid.api.PdfiumSource
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU.Companion.lock
import kotlinx.coroutines.CoroutineDispatcher

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
    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(parcelFileDescriptor: ParcelFileDescriptor): PdfDocumentKt =
        wrapSuspend(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(parcelFileDescriptor), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        parcelFileDescriptor: ParcelFileDescriptor,
        password: String?,
    ): PdfDocumentKt =
        wrapSuspend(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(parcelFileDescriptor, password), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: ByteArray?): PdfDocumentKt =
        wrapSuspend(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        data: ByteArray?,
        password: String?,
    ): PdfDocumentKt =
        wrapSuspend(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data, password), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: PdfiumSource): PdfDocumentKt =
        wrapSuspend(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        data: PdfiumSource,
        password: String?,
    ): PdfDocumentKt =
        wrapSuspend(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data, password), dispatcher)
        }

    fun setLockManager(lockManager: LockManager) {
        lock = lockManager
    }
}
