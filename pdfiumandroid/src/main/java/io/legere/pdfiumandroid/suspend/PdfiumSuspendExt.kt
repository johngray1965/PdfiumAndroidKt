package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.unlocked.PdfiumCoreU.Companion.lock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend inline fun <reified T> wrapSuspend(
    dispatcher: CoroutineDispatcher,
    crossinline block: () -> T,
): T =
    withContext(dispatcher) {
        lock.withLock {
            block()
        }
    }
