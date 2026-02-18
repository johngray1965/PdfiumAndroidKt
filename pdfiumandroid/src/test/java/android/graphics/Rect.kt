@file:Suppress("unused")

package android.graphics

data class Rect(
    @JvmField var left: Int = 0,
    @JvmField var top: Int = 0,
    @JvmField var right: Int = 0,
    @JvmField var bottom: Int = 0,
) {
    fun set(
        l: Int,
        t: Int,
        r: Int,
        b: Int,
    ) {
        left = l
        top = t
        right = r
        bottom = b
    }

    fun width() = right - left

    fun height() = bottom - top
}
