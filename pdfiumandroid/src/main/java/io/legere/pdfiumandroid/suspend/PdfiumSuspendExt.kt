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

package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU.Companion.lock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Suspends the current coroutine and executes the given [block] within the specified
 * [CoroutineDispatcher], ensuring exclusive access to a shared resource using a lock.
 *
 * This function is designed to safely execute potentially long-running or blocking PDFium
 * operations in a suspendable manner, preventing concurrent access to critical native resources.
 *
 * @param T The return type of the [block].
 * @param dispatcher The [CoroutineDispatcher] on which the [block] will be executed.
 *                   This should typically be an IO dispatcher for native calls.
 * @param block The suspendable block of code to execute. This block will be run inside
 *              a `withLock` block on a shared mutex to ensure thread safety with native calls.
 * @return The result of the [block] execution.
 */
suspend inline fun <reified T> wrapSuspend(
    dispatcher: CoroutineDispatcher,
    crossinline block: () -> T,
): T =
    withContext(dispatcher) {
        lock.withLock {
            block()
        }
    }
