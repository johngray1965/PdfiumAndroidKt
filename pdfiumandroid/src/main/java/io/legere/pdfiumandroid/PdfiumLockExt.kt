package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.unlocked.PdfiumCoreU

inline fun <reified T> wrapLock(crossinline block: () -> T): T =
    PdfiumCoreU.lock.withLockBlocking {
        block()
    }
