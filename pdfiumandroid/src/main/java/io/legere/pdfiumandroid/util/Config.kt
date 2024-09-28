package io.legere.pdfiumandroid.util

import androidx.annotation.Keep
import io.legere.pdfiumandroid.DefaultLogger
import io.legere.pdfiumandroid.LoggerInterface

var pdfiumConfig = Config()

@Keep
enum class AlreadyClosedBehavior {
    EXCEPTION,
    IGNORE,
}

@Keep
data class Config(
    val logger: LoggerInterface = DefaultLogger(),
    val alreadyClosedBehavior: AlreadyClosedBehavior = AlreadyClosedBehavior.EXCEPTION,
)

fun handleAlreadyClosed(isClosed: Boolean): Boolean {
    if (isClosed) {
        when (pdfiumConfig.alreadyClosedBehavior) {
            AlreadyClosedBehavior.EXCEPTION -> error("Already closed")
            AlreadyClosedBehavior.IGNORE ->
                pdfiumConfig.logger.d(
                    "PdfiumCore",
                    "Already closed",
                )
        }
    }
    return isClosed
}
