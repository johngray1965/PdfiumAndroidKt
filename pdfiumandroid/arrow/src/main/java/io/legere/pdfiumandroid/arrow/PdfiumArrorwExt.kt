/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023-2026 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.legere.pdfiumandroid.arrow

import arrow.core.Either
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU.Companion.lock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Executes a given [block] of code on the specified [CoroutineDispatcher] while acquiring
 * a global lock, and wraps the result in an [Either] type for robust error handling.
 *
 * This function is designed to handle potentially blocking or exception-throwing PDFium
 * operations safely within a coroutine. It ensures that native calls are made in a thread-safe
 * manner and that any exceptions thrown by the [block] are caught and converted into
 * [PdfiumKtFErrors] on the left side of the [Either].
 *
 * @param T The expected successful return type of the [block].
 * @param dispatcher The [CoroutineDispatcher] on which the [block] will be executed.
 *                   This should typically be an IO dispatcher for native calls.
 * @param block The suspendable block of code to execute. This block will be run inside
 *              a `withLock` block on a shared mutex to ensure thread safety with native calls.
 * @return An [Either] where:
 *         - `Left(PdfiumKtFErrors)` contains an error if the [block] throws an exception.
 *         - `Right(T)` contains the successful result of the [block] execution.
 */
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
