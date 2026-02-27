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

package io.legere.pdfiumandroid.core.util

import androidx.annotation.VisibleForTesting
import java.util.concurrent.CountDownLatch

class InitLock {
    private var latch = CountDownLatch(1)

    fun markReady() {
        latch.countDown() // Decrements count to 0, releasing all waiting threads
    }

    // We use a mutex to make sure only the
    // first thread waits on the semaphore
    @Synchronized
    fun waitForReady() {
        latch.await() // Blocks until count is 0. If already 0, returns immediately.
    }

    @VisibleForTesting
    @Synchronized
    fun reset() {
        latch = CountDownLatch(1)
    }
}
