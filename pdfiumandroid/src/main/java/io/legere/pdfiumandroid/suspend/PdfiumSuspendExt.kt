package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.util.pdfiumConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend inline fun <reified T> wrapSuspend(
    dispatcher: CoroutineDispatcher,
    crossinline block: () -> T,
): T =
    withContext(dispatcher) {
        pdfiumConfig.lock.withLock {
            block()
        }
    }
