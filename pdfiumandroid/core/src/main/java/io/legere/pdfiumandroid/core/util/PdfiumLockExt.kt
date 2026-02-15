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
