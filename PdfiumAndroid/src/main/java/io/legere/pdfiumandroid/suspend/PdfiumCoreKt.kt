@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.os.ParcelFileDescriptor
import io.legere.pdfiumandroid.PdfiumCore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * PdfiumCoreKt is the main entry-point for access to the PDFium API.
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 * @constructor create a [PdfiumCoreKt] from a [PdfiumCore]
 */
class PdfiumCoreKt(private val dispatcher: CoroutineDispatcher) {

    private val coreInternal = PdfiumCore()

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(fd: ParcelFileDescriptor): PdfDocumentKt {
        return withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(fd), dispatcher)
        }
    }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(fd: ParcelFileDescriptor, password: String?): PdfDocumentKt {
        return withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(fd, password), dispatcher)
        }
    }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: ByteArray?): PdfDocumentKt {
        return withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data), dispatcher)
        }
    }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: ByteArray?, password: String?): PdfDocumentKt {
        return withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data, password), dispatcher)
        }
    }
}
