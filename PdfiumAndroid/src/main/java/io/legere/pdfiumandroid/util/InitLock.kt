package io.legere.pdfiumandroid.util

import java.util.concurrent.Semaphore

class InitLock {

    private val semaphore = Semaphore(0)
    private var isInitialized = false

    fun markReady() {
        isInitialized = true
        semaphore.release()
    }

    fun waitForReady() {
        if (!isInitialized) {
            semaphore.acquire()
        }
    }
}
