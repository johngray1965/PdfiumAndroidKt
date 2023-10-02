package io.legere.pdfiumandroid

import android.util.Log

// At the moment we only do debug log with message, or error log with message and throwable
// in the future, we might expand this.
interface LoggerInterface {
    fun d(tag: String, message: String?)
    fun e(tag: String, t: Throwable?,  message: String?)
}

object Logger : LoggerInterface {
    private var logger: LoggerInterface? = null
    override fun d(tag: String, message: String?) {
        logger?.d(tag, message)
    }

    override fun e(tag: String, t: Throwable?, message: String?) {
        logger?.e(tag, t, message)
    }

    fun setLogger(logger: LoggerInterface) {
        this.logger = logger
    }
}

class DefaultLogger: LoggerInterface {
    override fun d(tag: String, message: String?) {
        message?.let { Log.d(tag, message) }

    }

    override fun e(tag: String, t: Throwable?, message: String?) {
        Log.e(tag, message, t)
    }
}
