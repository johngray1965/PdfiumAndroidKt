package io.legere.pdfiumandroid.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Semaphore

class InitLock {
    private val semaphore = Semaphore(0)
    private var isInitialized = false

    private val mutex = Mutex()

    fun markReady() {
        isInitialized = true
        semaphore.release()
    }

    // We use a mutex to make sure only the
    // first thread waits on the semaphore
    suspend fun waitForReady() {
        mutex.withLock {
            if (!isInitialized) {
                semaphore.acquire()
            }
        }
    }
}
