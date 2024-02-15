package io.legere.pdfiumandroid.util

import java.util.concurrent.Semaphore

class InitLock {
    private val semaphore = Semaphore(0)
    private var isInitialized = false

    fun markReady() {
        isInitialized = true
        semaphore.release()
    }

    // We use a mutex to make sure only the
    // first thread waits on the semaphore
    @Synchronized
    fun waitForReady() {
        if (!isInitialized) {
            semaphore.acquire()
        }
    }
}
