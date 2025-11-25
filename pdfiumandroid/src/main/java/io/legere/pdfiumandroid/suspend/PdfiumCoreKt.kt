@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.os.ParcelFileDescriptor
import androidx.annotation.Keep
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.PdfiumSource
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
import io.legere.pdfiumandroid.util.Config
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val coreInternal = PdfiumCoreU(config = config)

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(fd: ParcelFileDescriptor): PdfDocumentKt =
        mutex.withLock {
            withContext(dispatcher) {
                PdfDocumentKt(coreInternal.newDocument(fd), dispatcher)
            }
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        fd: ParcelFileDescriptor,
        password: String?,
    ): PdfDocumentKt =
        mutex.withLock {
            withContext(dispatcher) {
                PdfDocumentKt(coreInternal.newDocument(fd, password), dispatcher)
            }
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: ByteArray?): PdfDocumentKt =
        mutex.withLock {
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
        mutex.withLock {
            withContext(dispatcher) {
                PdfDocumentKt(coreInternal.newDocument(data, password), dispatcher)
            }
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: PdfiumSource): PdfDocumentKt =
        mutex.withLock {
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
        mutex.withLock {
            withContext(dispatcher) {
                PdfDocumentKt(coreInternal.newDocument(data, password), dispatcher)
            }
        }

    companion object {
        val mutex = Mutex()
    }
}
