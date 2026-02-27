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
