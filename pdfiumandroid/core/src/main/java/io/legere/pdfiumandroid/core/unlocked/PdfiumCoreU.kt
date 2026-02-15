@file:Suppress("unused")

package io.legere.pdfiumandroid.core.unlocked

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.VisibleForTesting
import io.legere.pdfiumandroid.api.Config
import io.legere.pdfiumandroid.api.LockManager
import io.legere.pdfiumandroid.api.LockManagerReentrantLock
import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.api.PdfiumSource
import io.legere.pdfiumandroid.api.pdfiumConfig
import io.legere.pdfiumandroid.core.jni.NativeCoreContract
import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.legere.pdfiumandroid.core.jni.defaultNativeFactory
import io.legere.pdfiumandroid.core.util.InitLock
import io.legere.pdfiumandroid.core.util.PdfiumNativeSourceBridge
import java.io.IOException

/**
 * PdfiumCoreU is the **unlocked** main entry-point for raw access to the PDFium API.
 * This class is for **internal use only** within the PdfiumAndroid library.
 * Direct use from outside the library is not recommended as it bypasses thread-safety mechanisms.
 *
 * It manages the loading of native libraries and provides methods to open PDF documents
 * from various sources without inherent thread-safety mechanisms at this layer.
 *
 * @property context The Android [Context] used to retrieve display metrics for DPI calculation.
 * @property config The [io.legere.pdfiumandroid.api.Config] object containing PdfiumAndroidKt's configuration.
 * @property nativeFactory The factory to provide native interface implementations.
 * @property libraryLoader The [LibraryLoader] responsible for loading native libraries.
 */
@Suppress("TooManyFunctions")
class PdfiumCoreU(
    context: Context? = null,
    val config: Config = Config(),
    val nativeFactory: NativeFactory = defaultNativeFactory,
    libraryLoader: LibraryLoader = SystemLibraryLoader,
) {
    private val nativeCore: NativeCoreContract = nativeFactory.getNativeCore()

    private val mCurrentDpi: Int

    init {
        // Trigger background loading if it hasn't started yet
        synchronized(lock) {
            if (!isLibraryLoaded) {
                Thread {
                    Log.d(TAG, "Loading native libraries in background thread")
                    @Suppress("TooGenericExceptionCaught")
                    try {
                        libraryLoader.load("pdfium")
                        libraryLoader.load("pdfiumandroid")
                        // Load success
                    } catch (e: Throwable) {
                        // Capture the error to throw on the main thread
                        libraryLoadError = e
                        Logger.e(TAG, e, "Native libraries failed to load")
                    } finally {
                        isReady.markReady() // Always release the latch
                        isLibraryLoaded = true
                    }
                }.start()
            }
        }

        pdfiumConfig = config
        Logger.setLogger(config.logger)
        Logger.d(TAG, "Starting PdfiumAndroid ")
        mCurrentDpi = context?.resources?.displayMetrics?.densityDpi ?: -1

        // Block until the background thread above completes the loading
        isReady.waitForReady()

        // FAIL FAST: If loading failed, re-throw the exception here on the calling thread.
        @Suppress("TooGenericExceptionThrown")
        libraryLoadError?.let {
            throw RuntimeException("Failed to initialize PdfiumCore native libraries", it)
        }
    }

    /**
     * Create new document from file descriptor.
     * For internal use only.
     *
     * @param fd opened file descriptor of file
     * @return [PdfDocumentU]
     * @throws IOException if the document cannot be opened
     */
    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor): PdfDocumentU = newDocument(fd, null)

    /**
     * Create new document from file descriptor with password.
     * For internal use only.
     *
     * @param parcelFileDescriptor opened file descriptor of file
     * @param password password for decryption
     * @return [PdfDocumentU]
     * @throws IOException if the document cannot be opened
     */
    @Throws(IOException::class)
    fun newDocument(
        parcelFileDescriptor: ParcelFileDescriptor,
        password: String?,
    ): PdfDocumentU =
        PdfDocumentU(
            nativeCore.openDocument(
                parcelFileDescriptor.fd,
                password,
            ),
            nativeFactory,
        ).also { document ->
            document.parcelFileDescriptor = parcelFileDescriptor
            document.source = null
        }

    /**
     * Create new document from bytearray.
     * For internal use only.
     *
     * @param data bytearray of pdf file
     * @return [PdfDocumentU]
     * @throws IOException if the document cannot be opened
     */
    @Throws(IOException::class)
    fun newDocument(data: ByteArray?): PdfDocumentU = newDocument(data, null)

    /**
     * Create new document from bytearray with password.
     * For internal use only.
     *
     * @param data bytearray of pdf file
     * @param password password for decryption
     * @return [PdfDocumentU]
     * @throws IOException if the document cannot be opened
     */
    @Throws(IOException::class)
    fun newDocument(
        data: ByteArray?,
        password: String?,
    ): PdfDocumentU =
        PdfDocumentU(nativeCore.openMemDocument(data, password), nativeFactory).also { document ->
            document.parcelFileDescriptor = null
            document.source = null
        }

    /**
     * Create new document from custom data source.
     * For internal use only.
     *
     * @param data custom data source to read from
     * @return [PdfDocumentU]
     * @throws IOException if the document cannot be opened
     */
    @Throws(IOException::class)
    fun newDocument(data: PdfiumSource): PdfDocumentU = newDocument(data, null)

    /**
     * Create new document from custom data source with password.
     * For internal use only.
     *
     * @param data custom data source to read from
     * @param password password for decryption
     * @return [PdfDocumentU]
     * @throws IOException if the document cannot be opened
     */
    @Throws(IOException::class)
    fun newDocument(
        data: PdfiumSource,
        password: String?,
    ): PdfDocumentU {
        val nativeSourceBridge = PdfiumNativeSourceBridge(data)
        return PdfDocumentU(
            nativeCore.openCustomDocument(
                nativeSourceBridge,
                password,
                data.length,
            ),
            nativeFactory,
        ).also { document ->
            document.parcelFileDescriptor = null
            document.source = data
        }
    }

    /**
     * Sets the global [io.legere.pdfiumandroid.api.LockManager] for PdfiumAndroidKt.
     * For internal use only.
     *
     * @param lockManager The [io.legere.pdfiumandroid.api.LockManager] implementation to use for
     * thread synchronization.
     */
    fun setLockManager(lockManager: LockManager) {
        lock = lockManager
    }

    /**
     * @suppress
     */
    companion object {
        private val TAG = PdfiumCoreU::class.java.name

        /** The global [io.legere.pdfiumandroid.api.LockManager] instance used for thread synchronization. */
        var lock: LockManager = LockManagerReentrantLock()

        /** An [InitLock] to signal when native libraries are ready. */
        internal val isReady = InitLock()

        // Flag to prevent double-loading
        private var isLibraryLoaded = false

        // Capture initialization failure
        private var libraryLoadError: Throwable? = null

        /**
         * Resets the static state for testing purposes.
         * For internal use only.
         *
         * Call this in your test setup or teardown to ensure a clean slate.
         */
        @VisibleForTesting
        fun resetForTesting() {
            synchronized(lock) {
                isLibraryLoaded = false
                libraryLoadError = null
                lock = LockManagerReentrantLock()
                // We need to recreate the InitLock because the old one might be counted down already
                // Note: In a real app, we never "unload", but for tests we need to "re-wait"
                // Reflectively swapping the val or assuming tests create new ClassLoaders't safe.
                // Since 'val isReady' cannot be reassigned, we have to be careful.

                // However, if a previous test FAILED, isReady is open (count=0), but libraryLoadError is set.
                // We just need to clear the error so the next test can try again.
                // But we also need to set isLibraryLoaded = false so the next test triggers the thread again.
                // AND we need a new Latch because the old one is already open (count=0).
            }
        }
    }
}
