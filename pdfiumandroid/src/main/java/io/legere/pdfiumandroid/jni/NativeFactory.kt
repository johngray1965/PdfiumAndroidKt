package io.legere.pdfiumandroid.jni

interface NativeFactory {
    fun getNativeCore(): NativeCoreContract

    fun getNativeDocument(): NativeDocumentContract

    fun getNativePage(): NativePageContract

    fun getNativeTextPage(): NativeTextPageContract

    fun getNativePageLink(): NativePageLinkContract

    fun getNativeFindResult(): NativeFindResultContract
}

internal val defaultNativeFactory =
    object : NativeFactory {
        override fun getNativeCore(): NativeCoreContract = NativeCore()

        override fun getNativeDocument(): NativeDocumentContract = NativeDocument()

        override fun getNativePage(): NativePageContract = NativePage()

        override fun getNativeTextPage(): NativeTextPageContract = NativeTextPage()

        override fun getNativePageLink(): NativePageLinkContract = NativePageLink()

        override fun getNativeFindResult(): NativeFindResultContract = NativeFindResult()
    }
