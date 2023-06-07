@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.content.Context
import android.os.ParcelFileDescriptor
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfiumCore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PdfiumCoreKt(private val dispatcher: CoroutineDispatcher, context: Context) {

    private val coreInternal = PdfiumCore(context)

    suspend fun newDocument(fd: ParcelFileDescriptor): PdfDocument {
        return withContext(dispatcher) {
            coreInternal.newDocument(fd)
        }
    }

    suspend fun newDocument(fd: ParcelFileDescriptor, password: String?): PdfDocument {
        return withContext(dispatcher) {
            coreInternal.newDocument(fd, password)
        }
    }

    suspend fun newDocument(data: ByteArray?): PdfDocument {
        return withContext(dispatcher) {
            coreInternal.newDocument(data)
        }
    }

    suspend fun newDocument(data: ByteArray?, password: String?): PdfDocument {
        return withContext(dispatcher) {
            coreInternal.newDocument(data, password)
        }
    }
}



