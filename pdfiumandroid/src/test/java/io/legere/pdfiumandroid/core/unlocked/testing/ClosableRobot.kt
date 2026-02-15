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
