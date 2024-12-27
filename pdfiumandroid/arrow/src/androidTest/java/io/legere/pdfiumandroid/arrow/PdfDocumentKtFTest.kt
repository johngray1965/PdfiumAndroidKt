package io.legere.pdfiumandroid.arrow

import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.raise.either
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.PdfWriteCallback
import io.legere.pdfiumandroid.arrow.base.BasePDFTest
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfDocumentKtFTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentKtF
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() =
        runBlocking {
            pdfBytes = getPdfBytes("f01.pdf")

            assertThat(pdfBytes).isNotNull()

            pdfDocument = PdfiumCoreKtF(Dispatchers.Unconfined).newDocument(pdfBytes).getOrNull()!!
        }

    @After
    fun tearDown() =
        runTest {
            pdfDocument.close()
        }

    @Test
    fun getPageCount() =
        runTest {
            either {
                val pageCount = pdfDocument.getPageCount().bind()

                assertThat(pageCount).isEqualTo(4)
            }
        }

    @Test
    fun openPage() =
        runTest {
            either {
                val page = pdfDocument.openPage(0).bind()

                assertThat(page).isNotNull()
            }
        }

    @Test
    fun openPages() =
        runTest {
            either {
                val page = pdfDocument.openPages(0, 3).bind()

                assertThat(page.size).isEqualTo(4)
            }
        }

    @Test
    fun getDocumentMeta() =
        runTest {
            either {
                val meta = pdfDocument.getDocumentMeta().bind()

                assertThat(meta).isNotNull()
            }
        }

    @Test
    fun getTableOfContents() =
        runTest {
            either {
                // I don't think this test document has a table of contents
                val toc = pdfDocument.getTableOfContents().bind()

                TestCase.assertNotNull(toc)
                assertThat(toc.size).isEqualTo(0)
            }
        }

    @Test
    fun openTextPage() =
        runTest {
            either {
                val page = pdfDocument.openPage(0).bind()
                val textPage = page.openTextPage().bind()
                assertThat(textPage).isNotNull()
            }
        }

    @Test
    fun openTextPages() =
        runTest {
            either {
                val textPages = pdfDocument.openTextPages(0, 3).bind()
                assertThat(textPages.size).isEqualTo(4)
            }
        }

    @Test
    fun saveAsCopy() =
        runTest {
            pdfDocument.saveAsCopy(
                object : PdfWriteCallback {
                    override fun WriteBlock(data: ByteArray?): Int {
                        // Truth.assertThat(data?.size).isEqualTo(pdfBytes?.size)
                        // Truth.assertThat(data).isEqualTo(pdfBytes)
                        return data?.size ?: 0
                    }
                },
            )
        }

    fun close() =
        runTest {
            either {
                var documentAfterClose: PdfDocumentKtF?
                PdfiumCoreKtF(Dispatchers.Unconfined).newDocument(pdfBytes).bind().use {
                    documentAfterClose = it
                }
                documentAfterClose?.openPage(0)
            }.mapLeft {
                assertThat(it).isInstanceOf(PdfiumKtFErrors.AlreadyClosed::class.java)
            }
        }
}
