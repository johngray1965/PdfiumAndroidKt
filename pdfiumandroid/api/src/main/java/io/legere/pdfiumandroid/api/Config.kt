package io.legere.pdfiumandroid.api

import androidx.annotation.Keep

var pdfiumConfig = Config()

/**
 * Defines the behavior when an operation is attempted on an already closed PDFium object.
 */
@Keep
enum class AlreadyClosedBehavior {
    /** Throws an [IllegalStateException] when an operation is attempted on a closed object. */
    EXCEPTION,

    /** Ignores the operation when an operation is attempted on a closed object. */
    IGNORE,

    /** Logs a debug message when an operation is attempted on a closed object. */
    LOG,
}

/**
 * Configuration class for the PdfiumAndroid library.
 *
 * @property logger The [io.legere.pdfiumandroid.api.LoggerInterface] implementation to use for
 * logging within the library.
 *                  Defaults to [io.legere.pdfiumandroid.api.DefaultLogger].
 * @property alreadyClosedBehavior Defines how the library reacts when an operation is attempted
 *                                 on a PDFium object that has already been closed.
 *                                 Defaults to [AlreadyClosedBehavior.EXCEPTION].
 */
@Keep
data class Config(
    val logger: LoggerInterface = DefaultLogger(),
    val alreadyClosedBehavior: AlreadyClosedBehavior = AlreadyClosedBehavior.EXCEPTION,
)

/**
 * Handles the scenario where an operation is attempted on an already closed object.
 * The behavior is determined by [pdfiumConfig.alreadyClosedBehavior].
 *
 * @param isClosed A boolean indicating whether the object is currently closed.
 * @return `true` if the object is closed, `false` otherwise.
 * @throws IllegalStateException if [pdfiumConfig.alreadyClosedBehavior] is
 * [AlreadyClosedBehavior.EXCEPTION] and `isClosed` is `true`.
 */
fun handleAlreadyClosed(isClosed: Boolean): Boolean {
    if (isClosed) {
        when (pdfiumConfig.alreadyClosedBehavior) {
            AlreadyClosedBehavior.EXCEPTION -> {
                error("Already closed")
            }

            AlreadyClosedBehavior.LOG -> {
                pdfiumConfig.logger.d(
                    "PdfiumCore",
                    "Already closed",
                )
            }

            AlreadyClosedBehavior.IGNORE -> {
                // do nothing
            }
        }
    }
    return isClosed
}
