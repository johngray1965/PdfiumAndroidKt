@file:Suppress("unused")

package io.legere.pdfiumandroid.unlocked

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfiumSource
import io.legere.pdfiumandroid.util.Config
import io.legere.pdfiumandroid.util.InitLock
import io.legere.pdfiumandroid.util.PdfiumNativeSourceBridge
import io.legere.pdfiumandroid.util.pdfiumConfig
import kotlinx.coroutines.sync.Mutex
import java.io.IOException

/**
 * PdfiumCore is the main entry-point for access to the PDFium API.
 */
@Suppress("TooManyFunctions")
class PdfiumCoreU(
    context: Context? = null,
    val config: Config = Config(),
) {
    private val mCurrentDpi: Int

    init {
        pdfiumConfig = config
        Logger.setLogger(config.logger)
        Logger.d(TAG, "Starting PdfiumAndroid ")
        mCurrentDpi = context?.resources?.displayMetrics?.densityDpi ?: -1
        isReady.waitForReady()
    }

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

    /**
     * Create new document from file
     * @param fd opened file descriptor of file
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor): PdfDocumentU = newDocument(fd, null)

    /**
     * Create new document from file with password
     * @param parcelFileDescriptor opened file descriptor of file
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(
        parcelFileDescriptor: ParcelFileDescriptor,
        password: String?,
    ): PdfDocumentU =
        PdfDocumentU(
            nativeOpenDocument(
                parcelFileDescriptor.fd,
                password,
            ),
        ).also { document ->
            document.parcelFileDescriptor = parcelFileDescriptor
            document.source = null
        }

    /**
     * Create new document from bytearray
     * @param data bytearray of pdf file
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(data: ByteArray?): PdfDocumentU = newDocument(data, null)

    /**
     * Create new document from bytearray with password
     * @param data bytearray of pdf file
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(
        data: ByteArray?,
        password: String?,
    ): PdfDocumentU =
        PdfDocumentU(nativeOpenMemDocument(data, password)).also { document ->
            document.parcelFileDescriptor = null
            document.source = null
        }

    /**
     * Create new document from custom data source
     * @param data custom data source to read from
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(data: PdfiumSource): PdfDocumentU = newDocument(data, null)

    /**
     * Create new document from custom data source with password
     * @param data custom data source to read from
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(
        data: PdfiumSource,
        password: String?,
    ): PdfDocumentU {
        val nativeSourceBridge = PdfiumNativeSourceBridge(data)
        return PdfDocumentU(
            nativeOpenCustomDocument(
                nativeSourceBridge,
                password,
                data.length,
            ),
        ).also { document ->
            document.parcelFileDescriptor = null
            document.source = data
        }
    }

    companion object {
        private val TAG = PdfiumCoreU::class.java.name

        // synchronize native methods
        val lock = Any()

        val surfaceMutex = Mutex()

        val isReady = InitLock()

        init {
            Log.d(TAG, "init")
            Thread {
                Log.d(TAG, "init thread start")
                synchronized(lock) {
                    Log.d(TAG, "init in lock")
                    try {
                        System.loadLibrary("pdfium")
                        System.loadLibrary("pdfiumandroid")
                        isReady.markReady()
                    } catch (e: UnsatisfiedLinkError) {
                        Logger.e(TAG, e, "Native libraries failed to load")
                    }
                    Log.d(TAG, "init in lock")
                }
            }.start()
        }
    }
}
