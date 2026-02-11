package io.legere.pdfiumandroid

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LockManager {
    private val mutex = Mutex()

    /**
     * The Single Source of Truth for locking.
     * All suspend API calls must pass through this.
     */
    suspend fun <T> withLock(block: suspend () -> T): T =
        mutex.withLock {
            block()
        }

    /**
     * The Bridge for non-suspend callers.
     * Centralizes 'runBlocking' so you can change it globally later.
     */
    fun <T> withLockBlocking(block: () -> T): T =
        runBlocking {
            mutex.withLock {
                block()
            }
        }
}
