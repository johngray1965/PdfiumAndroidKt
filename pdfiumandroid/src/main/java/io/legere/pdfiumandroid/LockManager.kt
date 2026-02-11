package io.legere.pdfiumandroid

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.locks.ReentrantLock

interface LockManager {
    suspend fun <T> withLock(block: suspend () -> T): T

    fun <T> withLockBlocking(block: () -> T): T
}

class LockManagerReentrantLockImpl : LockManager {
    private val lock = ReentrantLock()

    /**
     * Executes the given [block] function while holding the lock.
     */
    override suspend fun <T> withLock(block: suspend () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
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

    fun status() = lock.isLocked
}

class LockManagerSplitLockImpl : LockManager {
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

    fun status() = lock.isLocked
}

class LockManagerSuspendOnlyImpl : LockManager {
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

    fun status() = lock.isLocked
}
