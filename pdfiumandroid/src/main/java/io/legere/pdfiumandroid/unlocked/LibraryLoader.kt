package io.legere.pdfiumandroid.unlocked

internal interface LibraryLoader {
    fun load(libName: String)
}

internal object SystemLibraryLoader : LibraryLoader {
    override fun load(libName: String) {
        System.loadLibrary(libName)
    }
}
