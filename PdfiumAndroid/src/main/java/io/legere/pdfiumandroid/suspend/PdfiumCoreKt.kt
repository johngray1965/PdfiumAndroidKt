@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.content.Context
import android.os.ParcelFileDescriptor
import io.legere.pdfiumandroid.PdfiumCore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PdfiumCoreKt(private val dispatcher: CoroutineDispatcher, context: Context) {

    private val coreInternal = PdfiumCore(context)

    suspend fun newDocument(fd: ParcelFileDescriptor): PdfDocumentKt {
        return withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(fd), dispatcher)
        }
    }

    suspend fun newDocument(fd: ParcelFileDescriptor, password: String?): PdfDocumentKt {
        return withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(fd, password), dispatcher)
        }
    }

    suspend fun newDocument(data: ByteArray?): PdfDocumentKt {
        return withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data), dispatcher)
        }
    }

    suspend fun newDocument(data: ByteArray?, password: String?): PdfDocumentKt {
        return withContext(dispatcher) {
            PdfDocumentKt(coreInternal.newDocument(data, password), dispatcher)
        }
    }
}



