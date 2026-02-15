package io.legere.pdfiumandroid.api

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.locks.ReentrantLock

/**
 * Lock manager interface.
 */
interface LockManager {
    /**
     * Executes the given [block] function while holding the lock.
     */
    suspend fun <T> withLock(block: suspend () -> T): T

    /**
     * Executes the given [block] function while holding the lock.
     */
    fun <T> withLockBlocking(block: () -> T): T

    /**
     * Returns the status of the lock.
     */
    fun status(): Boolean
}

class LockManagerReentrantLock : LockManager {
    private val lock = ReentrantLock()

    override suspend fun <T> withLock(block: suspend () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }

    override fun <T> withLockBlocking(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }

    override fun status() = lock.isLocked
}

class LockManagerSplitLock : LockManager {
    private val lock = ReentrantLock()
    private val mutex = Mutex()

    /**
     * Executes the given [block] function while holding the lock.
     */
    override suspend fun <T> withLock(block: suspend () -> T): T {
        mutex.withLock {
            return block()
        }
    }

    /**
     * Executes the given [block] function while holding the lock.
     */
    override fun <T> withLockBlocking(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }

    override fun status() = lock.isLocked
}

class LockManagerSuspendOnly : LockManager {
    private val lock = ReentrantLock()
    private val mutex = Mutex()

    /**
     * Executes the given [block] function while holding the lock.
     */
    override suspend fun <T> withLock(block: suspend () -> T): T {
        mutex.withLock {
            return block()
        }
    }

    /**
     * Executes the given [block] function while holding the lock.
     */
    override fun <T> withLockBlocking(block: () -> T): T {
        error("Not supported")
    }

    override fun status() = lock.isLocked
}

class LockManagerSuspendWithBlocking : LockManager {
    private val lock = ReentrantLock()
    private val mutex = Mutex()

    /**
     * Executes the given [block] function while holding the lock.
     */
    override suspend fun <T> withLock(block: suspend () -> T): T {
        mutex.withLock {
            return block()
        }
    }

    /**
     * Executes the given [block] function while holding the lock.
     */
    override fun <T> withLockBlocking(block: () -> T): T =
        runBlocking {
            withLock(block)
        }

    override fun status() = lock.isLocked
}
