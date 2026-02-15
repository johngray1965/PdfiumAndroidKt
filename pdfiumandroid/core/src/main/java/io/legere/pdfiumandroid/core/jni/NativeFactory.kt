package io.legere.pdfiumandroid.core.jni

/**
 * Factory interface for providing implementations of native PDFium contracts.
 * This allows for dependency injection and easier testing of classes that interact
 * with the native layer. Implementations of this factory are intended for **internal use only**
 * within the PdfiumAndroid library.
 */
interface NativeFactory {
    /**
     * Provides an instance of [NativeCoreContract] for core native operations.
     * @return An implementation of [NativeCoreContract].
     */
    fun getNativeCore(): NativeCoreContract

    /**
     * Provides an instance of [NativeDocumentContract] for native document operations.
     * @return An implementation of [NativeDocumentContract].
     */
    fun getNativeDocument(): NativeDocumentContract

    /**
     * Provides an instance of [NativePageContract] for native page operations.
     * @return An implementation of [NativePageContract].
     */
    fun getNativePage(): NativePageContract

    /**
     * Provides an instance of [NativeTextPageContract] for native text page operations.
     * @return An implementation of [NativeTextPageContract].
     */
    fun getNativeTextPage(): NativeTextPageContract

    /**
     * Provides an instance of [NativePageLinkContract] for native page link operations.
     * @return An implementation of [NativePageLinkContract].
     */
    fun getNativePageLink(): NativePageLinkContract

    /**
     * Provides an instance of [NativeFindResultContract] for native find result operations.
     * @return An implementation of [NativeFindResultContract].
     */
    fun getNativeFindResult(): NativeFindResultContract
}

val defaultNativeFactory =
    object : NativeFactory {
        override fun getNativeCore(): NativeCoreContract = NativeCore()

        override fun getNativeDocument(): NativeDocumentContract = NativeDocument()

        override fun getNativePage(): NativePageContract = NativePage()

        override fun getNativeTextPage(): NativeTextPageContract = NativeTextPage()

        override fun getNativePageLink(): NativePageLinkContract = NativePageLink()

        override fun getNativeFindResult(): NativeFindResultContract = NativeFindResult()
    }
