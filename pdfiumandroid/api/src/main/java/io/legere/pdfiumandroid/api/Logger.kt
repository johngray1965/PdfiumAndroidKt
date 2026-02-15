package io.legere.pdfiumandroid.api

import android.util.Log
import androidx.annotation.Keep

/**
 * Interface for logging within the PdfiumAndroid library. This allows for custom
 * logger implementations to be plugged in.
 */
@Keep
interface LoggerInterface {
    /**
     * Logs a debug message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies
     * the class or activity where the log call occurs.
     * @param message The message to log.
     */
    fun d(
        tag: String,
        message: String?,
    )

    /**
     * Logs an error message, optionally with a [Throwable].
     *
     * @param tag Used to identify the source of a log message. It usually identifies
     * the class or activity where the log call occurs.
     * @param t An optional [Throwable] to be logged.
     * @param message The message to log.
     */
    fun e(
        tag: String,
        t: Throwable?,
        message: String?,
    )
}

@Suppress("MemberNameEqualsClassName")
object Logger : LoggerInterface {
    private var logger: LoggerInterface? = null

    override fun d(
        tag: String,
        message: String?,
    ) {
        logger?.d(tag, message)
    }

    override fun e(
        tag: String,
        t: Throwable?,
        message: String?,
    ) {
        logger?.e(tag, t, message)
    }

    fun setLogger(logger: LoggerInterface) {
        this.logger = logger
    }
}

class DefaultLogger : LoggerInterface {
    override fun d(
        tag: String,
        message: String?,
    ) {
        message?.let { Log.d(tag, message) }
    }

    override fun e(
        tag: String,
        t: Throwable?,
        message: String?,
    ) {
        Log.e(tag, message, t)
    }
}
