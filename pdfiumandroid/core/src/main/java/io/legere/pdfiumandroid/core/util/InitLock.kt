package io.legere.pdfiumandroid.core.util

import java.util.concurrent.CountDownLatch

class InitLock {
    private val latch = CountDownLatch(1)

    fun markReady() {
        latch.countDown() // Decrements count to 0, releasing all waiting threads
    }

    // We use a mutex to make sure only the
    // first thread waits on the semaphore
    @Synchronized
    fun waitForReady() {
        latch.await() // Blocks until count is 0. If already 0, returns immediately.
    }
}
