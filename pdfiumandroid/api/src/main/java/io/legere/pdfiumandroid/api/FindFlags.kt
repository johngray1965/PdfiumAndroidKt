package io.legere.pdfiumandroid.api

@Suppress("MagicNumber")
enum class FindFlags(
    val value: Int,
) {
    MatchCase(0x00000001),
    MatchWholeWord(0x00000002),
    Consecutive(0x00000004),
}
