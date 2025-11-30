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
