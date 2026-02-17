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

package io.legere.pdfiumandroid.testing

interface ClosableTestContext {
    fun shouldReturnDefault(): Boolean

    fun setupRules()
}

fun <T> ClosableTestContext.nullableTest(func: NullableRobot<T>.() -> Unit) {
    val robot = NullableRobot<T>(shouldReturnDefault())
    robot.func()
    robot.execute()
}

class NullableRobot<T>(
    private val shouldDefault: Boolean,
) {
    lateinit var apiCall: () -> T
    private var verifyHappy: ((T) -> Unit)? = null
    private var verifyDefault: ((T) -> Unit)? = null
    private var setup: (() -> Unit)? = null

    fun setup(block: () -> Unit) {
        setup = block
    }

    fun verifyHappy(block: (T) -> Unit) {
        verifyHappy = block
    }

    fun verifyDefault(block: (T) -> Unit) {
        verifyDefault = block
    }

    fun execute() {
        if (shouldDefault) {
            val result = apiCall()
            verifyDefault?.invoke(result)
        } else {
            setup?.invoke()
            val result = apiCall()
            verifyHappy?.invoke(result)
        }
    }
}
