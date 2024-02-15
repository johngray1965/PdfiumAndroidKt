package io.legere.pdfiumandroid.arrow

import arrow.core.Either
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend inline fun <reified T> wrapEither(
    dispatcher: CoroutineDispatcher,
    crossinline block: () -> T
): Either<PdfiumKtFErrors, T> {
    return withContext(dispatcher) {
        Either.catch {
            block()
        }.mapLeft {
            exceptionToPdfiumKtFError(it)
        }
    }
}
