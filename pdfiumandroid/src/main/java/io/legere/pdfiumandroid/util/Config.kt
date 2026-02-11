package io.legere.pdfiumandroid.util

import androidx.annotation.Keep
import io.legere.pdfiumandroid.DefaultLogger
import io.legere.pdfiumandroid.LockManager
import io.legere.pdfiumandroid.LockManagerReentrantLockImpl
import io.legere.pdfiumandroid.LoggerInterface

var pdfiumConfig = Config()

@Keep
enum class AlreadyClosedBehavior {
    EXCEPTION,
    IGNORE,
    LOG,
}

@Keep
data class Config(
    val logger: LoggerInterface = DefaultLogger(),
    val alreadyClosedBehavior: AlreadyClosedBehavior = AlreadyClosedBehavior.EXCEPTION,
    val lock: LockManager = LockManagerReentrantLockImpl(),
)

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
