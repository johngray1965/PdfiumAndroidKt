package io.legere.pdfiumandroid.arrow

import arrow.core.Either
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU.Companion.lock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend inline fun <reified T> wrapEither(
    dispatcher: CoroutineDispatcher,
    crossinline block: () -> T,
): Either<PdfiumKtFErrors, T> =
    withContext(dispatcher) {
        lock.withLock {
            Either
                .catch {
                    block()
                }.mapLeft {
                    exceptionToPdfiumKtFError(it)
                }
        }
    }
