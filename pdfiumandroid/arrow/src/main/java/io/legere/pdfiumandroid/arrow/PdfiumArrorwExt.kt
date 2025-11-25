package io.legere.pdfiumandroid.arrow

import arrow.core.Either
import io.legere.pdfiumandroid.suspend.PdfiumCoreKt.Companion.mutex
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

suspend inline fun <reified T> wrapEither(
    dispatcher: CoroutineDispatcher,
    crossinline block: () -> T,
): Either<PdfiumKtFErrors, T> =
    mutex.withLock {
        withContext(dispatcher) {
            Either
                .catch {
                    block()
                }.mapLeft {
                    exceptionToPdfiumKtFError(it)
                }
        }
    }
