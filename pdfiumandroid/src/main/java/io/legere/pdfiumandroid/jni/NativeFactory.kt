package io.legere.pdfiumandroid.jni

interface NativeFactory {
    fun getNativeCore(): NativeCore

    fun getNativeDocument(): NativeDocument

    fun getNativePage(): NativePage

    fun getNativeTextPage(): NativeTextPage

    fun getNativePageLink(): NativePageLink

    fun getNativeFindResult(): NativeFindResult
}

val defaultNativeFactory =
    object : NativeFactory {
        override fun getNativeCore(): NativeCore = NativeCore()

        override fun getNativeDocument(): NativeDocument = NativeDocument()

        override fun getNativePage(): NativePage = NativePage()

        override fun getNativeTextPage(): NativeTextPage = NativeTextPage()

        override fun getNativePageLink(): NativePageLink = NativePageLink()

        override fun getNativeFindResult(): NativeFindResult = NativeFindResult()
    }
