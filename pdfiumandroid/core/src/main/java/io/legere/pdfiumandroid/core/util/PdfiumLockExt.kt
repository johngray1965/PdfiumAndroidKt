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

package io.legere.pdfiumandroid.core.util

import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU

/**
 * Executes the given [block] of code while holding a reentrant lock, ensuring thread-safe
 * access to shared resources for PDFium operations.
 *
 * This function uses a blocking lock (`withLockBlocking`) and is suitable for synchronous
 * operations that require exclusive access. For suspending (asynchronous) operations,
 * consider using [io.legere.pdfiumandroid.suspend.wrapSuspend].
 *
 * @param T The return type of the [block].
 * @param block The block of code to execute. This block will be run inside a
 *              `withLockBlocking` block on a shared mutex to ensure thread safety with native calls.
 * @return The result of the [block] execution.
 */
inline fun <reified T> wrapLock(crossinline block: () -> T): T =
    PdfiumCoreU.lock.withLockBlocking {
        block()
    }
