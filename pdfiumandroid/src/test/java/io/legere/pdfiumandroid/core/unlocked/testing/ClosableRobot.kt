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

package io.legere.pdfiumandroid.core.unlocked.testing

import org.junit.jupiter.api.assertThrows

interface ClosableTestContext {
    fun shouldThrowException(): Boolean

    fun shouldReturnDefault(): Boolean
}

fun <T> ClosableTestContext.closableTest(func: ClosableRobot<T>.() -> Unit) {
    val robot = ClosableRobot<T>(shouldThrowException(), shouldReturnDefault())
    robot.func()
    robot.execute()
}

class ClosableRobot<T>(
    private val shouldThrow: Boolean,
    private val shouldDefault: Boolean,
) {
    lateinit var apiCall: () -> T
    private var verifyHappy: ((T) -> Unit)? = null
    private var verifyDefault: ((T) -> Unit)? = null
    private var setupHappy: (() -> Unit)? = null

    fun setupHappy(block: () -> Unit) {
        setupHappy = block
    }

    fun verifyHappy(block: (T) -> Unit) {
        verifyHappy = block
    }

    fun verifyDefault(block: (T) -> Unit) {
        verifyDefault = block
    }

    fun execute() {
        if (shouldThrow) {
            assertThrows<Exception> {
                apiCall()
            }
        } else if (shouldDefault) {
            val result = apiCall()
            verifyDefault?.invoke(result)
        } else {
            setupHappy?.invoke()
            val result = apiCall()
            verifyHappy?.invoke(result)
        }
    }
}
