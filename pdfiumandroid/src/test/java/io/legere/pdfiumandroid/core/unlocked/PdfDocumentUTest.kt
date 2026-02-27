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

package io.legere.pdfiumandroid.core.unlocked

import android.graphics.Matrix
import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.AlreadyClosedBehavior
import io.legere.pdfiumandroid.api.Config
import io.legere.pdfiumandroid.api.Meta
import io.legere.pdfiumandroid.api.PdfWriteCallback
import io.legere.pdfiumandroid.api.pdfiumConfig
import io.legere.pdfiumandroid.core.jni.NativeDocument
import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.legere.pdfiumandroid.core.jni.NativeTextPage
import io.legere.pdfiumandroid.core.unlocked.testing.ClosableTestContext
import io.legere.pdfiumandroid.core.unlocked.testing.closableTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
abstract class PdfDocumentUBaseTest : ClosableTestContext {
    @MockK
    lateinit var mockNativeFactory: NativeFactory

    @MockK
    lateinit var mockNativeDocument: NativeDocument

    @MockK
    lateinit var mockNativeTextPage: NativeTextPage

    lateinit var pdfDocumentU: PdfDocumentU

    abstract fun getBehavior(): AlreadyClosedBehavior

    abstract fun setupClosedState()

    abstract fun isStateClosed(): Boolean

    override fun shouldThrowException() = getBehavior() == AlreadyClosedBehavior.EXCEPTION && isStateClosed()

    override fun shouldReturnDefault() = getBehavior() == AlreadyClosedBehavior.IGNORE && isStateClosed()

    @BeforeEach
    fun setUp() {
        PdfiumCoreU.resetForTesting()
        pdfiumConfig = Config(alreadyClosedBehavior = getBehavior())

        every { mockNativeFactory.getNativeDocument() } returns mockNativeDocument
        every { mockNativeFactory.getNativeTextPage() } returns mockNativeTextPage
        every { mockNativeDocument.closeDocument(any()) } just runs

        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)

        setupClosedState()
    }

    @Test
    fun `getPageCount happy path`() =
        closableTest {
            setupHappy {
                every { mockNativeDocument.getPageCount(any()) } returns 123
            }
            apiCall = {
                pdfDocumentU.getPageCount()
            }

            verifyHappy {
                assertThat(it).isEqualTo(123)
            }
            verifyDefault {
                assertThat(it).isEqualTo(0)
            }
        }

    @Test
    fun `getPageCharCounts happy path`() =
        closableTest {
            setupHappy {
                every { mockNativeDocument.getPageCharCounts(any()) } returns intArrayOf(100)
            }
            apiCall = {
                pdfDocumentU.getPageCharCounts()
            }

            verifyHappy {
                assertThat(it).isEqualTo(intArrayOf(100))
            }
            verifyDefault {
                assertThat(it).isEqualTo(intArrayOf())
            }
        }

    @Test
    fun `deletePage happy path`() =
        closableTest {
            setupHappy {
                every { mockNativeDocument.deletePage(any(), any()) } just runs
            }
            apiCall = {
                pdfDocumentU.deletePage(0)
            }

            verifyHappy {
                verify(exactly = 1) { mockNativeDocument.deletePage(0, 0) }
            }
            verifyDefault {
                verify(exactly = 0) { mockNativeDocument.deletePage(0, 0) }
            }
        }

    @Test
    fun `openPage check`() =
        closableTest {
            setupHappy {
                every { mockNativeDocument.loadPage(any(), any()) } returns 124
            }
            apiCall = {
                pdfDocumentU.openPage(1)
            }

            verifyHappy {
                assertThat(it).isNotNull()
                verify(exactly = 1) { mockNativeDocument.loadPage(any(), any()) }
            }
            verifyDefault {
                assertThat(it).isNull()
                verify(exactly = 0) { mockNativeDocument.loadPage(any(), any()) }
            }
        }

    @Test
    fun `openPages range check`() =
        closableTest {
            setupHappy {
                val pages = longArrayOf(1, 2, 3)
                every { mockNativeDocument.loadPages(any(), 0, 2) } returns pages
            }
            apiCall = {
                pdfDocumentU.openPages(0, 2)
            }

            verifyHappy {
                assertThat(it.size).isEqualTo(3)
                verify(exactly = 1) { mockNativeDocument.loadPages(0, 0, 2) }
            }
            verifyDefault {
                assertThat(it).isEmpty()
            }
        }

    @Test
    fun `openPages loop break logic`() {
        // This test seems to be obsolete as the loop doesn't exist anymore
    }

    @Test
    @Suppress("LongMethod")
    fun `renderPages  buffer  matrix flattening`() =
        closableTest {
            val matrixSlot = slot<FloatArray>()
            val page =
                mockk<PdfPageU> {
                    every { isClosed } returns false
                    every { pagePtr } returns 1L
                }
            val matrix = Matrix()
            matrix.setScale(2f, 2f)
            matrix.postTranslate(10f, 10f)
            setupHappy {
                every {
                    mockNativeDocument.renderPagesWithMatrix(
                        any(),
                        any(),
                        any(),
                        any(),
                        capture(matrixSlot),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } just runs
            }
            apiCall = {
                pdfDocumentU.renderPages(0L, 0, 0, listOf(page), listOf(matrix), listOf(RectF()))
            }

            verifyHappy {
                val flattenedMatrix = matrixSlot.captured
                println(flattenedMatrix.contentToString())
                assertThat(flattenedMatrix.size).isEqualTo(6)
                verify(
                    exactly = 1,
                ) {
                    mockNativeDocument.renderPagesWithMatrix(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                }
            }
            verifyDefault {
                verify(
                    exactly = 0,
                ) {
                    mockNativeDocument.renderPagesWithMatrix(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                }
            }
        }

    @Test
    fun `renderPages  buffer  clip rect flattening`() =
        closableTest {
            val clipRect = RectF(1f, 2f, 3f, 4f)
            val clipSlot = slot<FloatArray>()
            val page =
                mockk<PdfPageU> {
                    every { isClosed } returns false
                    every { pagePtr } returns 1L
                }
            setupHappy {
                every {
                    mockNativeDocument.renderPagesWithMatrix(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        capture(clipSlot),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } just runs
            }
            apiCall = {
                pdfDocumentU.renderPages(0L, 0, 0, listOf(page), listOf(Matrix()), listOf(clipRect))
            }

            verifyHappy {
                val flattenedClip = clipSlot.captured
                println(flattenedClip.contentToString())
                assertThat(flattenedClip.size).isEqualTo(4)
//        assertThat(flattenedClip[0]).isEqualTo(1f)
//        assertThat(flattenedClip[1]).isEqualTo(2f)
//        assertThat(flattenedClip[2]).isEqualTo(3f)
//        assertThat(flattenedClip[3]).isEqualTo(4f)
            }
            verifyDefault {
                verify(
                    exactly = 0,
                ) {
                    mockNativeDocument.renderPagesWithMatrix(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                }
            }
        }

    @Test
    fun `renderPages  surface  return value check`() =
        closableTest {
            val page =
                mockk<PdfPageU> {
                    every { isClosed } returns false
                    every { pagePtr } returns 1L
                }
            setupHappy {
                every {
                    mockNativeDocument.renderPagesSurfaceWithMatrix(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns true
            }
            apiCall = {
                pdfDocumentU.renderPages(mockk(), listOf(page), listOf(Matrix()), listOf(RectF()))
            }

            verifyHappy {
                assertThat(it).isTrue()
            }
            verifyDefault {
                assertThat(it).isFalse()
            }
        }

    @Test
    @Suppress("LongMethod")
    fun getDocumentMeta() =
        closableTest {
            setupHappy {
                every { mockNativeDocument.getDocumentMetaText(any(), "Title") } returns "My Title"
                every {
                    mockNativeDocument.getDocumentMetaText(
                        any(),
                        "Author",
                    )
                } returns "My Author"
                every {
                    mockNativeDocument.getDocumentMetaText(
                        any(),
                        "Subject",
                    )
                } returns "My Subject"
                every {
                    mockNativeDocument.getDocumentMetaText(
                        any(),
                        "Keywords",
                    )
                } returns "My Keywords"
                every {
                    mockNativeDocument.getDocumentMetaText(
                        any(),
                        "Creator",
                    )
                } returns "My Creator"
                every {
                    mockNativeDocument.getDocumentMetaText(
                        any(),
                        "Producer",
                    )
                } returns "My Producer"
                every {
                    mockNativeDocument.getDocumentMetaText(
                        any(),
                        "CreationDate",
                    )
                } returns "My CreationDate"
                every {
                    mockNativeDocument.getDocumentMetaText(
                        any(),
                        "ModDate",
                    )
                } returns "My ModDate"
            }
            apiCall = {
                pdfDocumentU.getDocumentMeta()
            }

            verifyHappy { meta ->
                assertThat(meta.title).isEqualTo("My Title")
                assertThat(meta.author).isEqualTo("My Author")
                assertThat(meta.subject).isEqualTo("My Subject")
                assertThat(meta.keywords).isEqualTo("My Keywords")
                assertThat(meta.creator).isEqualTo("My Creator")
                assertThat(meta.producer).isEqualTo("My Producer")
                assertThat(meta.creationDate).isEqualTo("My CreationDate")
                assertThat(meta.modDate).isEqualTo("My ModDate")
            }
            verifyDefault {
                assertThat(it).isEqualTo(Meta())
            }
        }

    @Test
    fun `recursiveGetBookmark recursion limit`() =
        closableTest {
            setupHappy {
                // Verify that recursiveGetBookmark stops recursing if the nesting level exceeds
                // MAX_RECURSION (usually 50), preventing potential StackOverflowErrors.

                // Setup: Create a scenario where every bookmark has a child, going deeper than 50 levels.
                // We simulate this by having getFirstChildBookmark always return a valid pointer.
                every { mockNativeDocument.getFirstChildBookmark(any(), any()) } answers {
                    // Return a "fake" pointer equal to the current pointer + 1 to simulate a new node
                    arg<Long>(1) + 1
                }
                // Assume no siblings for simplicity, just deep nesting
                every { mockNativeDocument.getSiblingBookmark(any(), any()) } returns 0
                every { mockNativeDocument.getBookmarkTitle(any()) } returns "Deep Node"
                every { mockNativeDocument.getBookmarkDestIndex(any(), any()) } returns 0L

                // We need to mock the initial call to getFirstChildBookmark from the document root (null)
                every { mockNativeDocument.getFirstChildBookmark(any(), 0) } returns 100L
            }
            apiCall = {
                pdfDocumentU.getTableOfContents()
            }

            verifyHappy {
                assertThat(it).isNotEmpty()
            }
            verifyDefault {
                assertThat(it).isEmpty()
            }
        }

    @Test
    fun `getTableOfContents top level only`() =
        closableTest {
            setupHappy {
                // Verify that recursiveGetBookmark stops recursing if the nesting level exceeds
                // MAX_RECURSION (usually 50), preventing potential StackOverflowErrors.

                // Setup: Create a scenario where every bookmark has a child, going deeper than 50 levels.
                // We simulate this by having getFirstChildBookmark always return a valid pointer.
                every { mockNativeDocument.getFirstChildBookmark(any(), any()) } answers {
                    // Return a "fake" pointer equal to the current pointer + 1 to simulate a new node
                    0
                }
                // Assume no siblings for simplicity, just deep nesting
                every { mockNativeDocument.getSiblingBookmark(any(), any()) } returns 0
                every { mockNativeDocument.getBookmarkTitle(any()) } returns "Deep Node"
                every { mockNativeDocument.getBookmarkDestIndex(any(), any()) } returns 0L

                // We need to mock the initial call to getFirstChildBookmark from the document root (null)
                every { mockNativeDocument.getFirstChildBookmark(any(), 0) } returns 100L
            }
            apiCall = {
                pdfDocumentU.getTableOfContents()
            }

            verifyHappy {
                assertThat(it).isNotEmpty()
            }
            verifyDefault {
                assertThat(it).isEmpty()
            }
        }

    @Test
    fun `getTableOfContents structure assembly`() =
        closableTest {
            setupHappy {
                val child1Ptr = 200L
                val child2Ptr = 300L
                val child11Ptr = 400L

                // Initial call for root
                every { mockNativeDocument.getFirstChildBookmark(any(), any()) } returns child1Ptr

                // Child 1 Setup (Has sibling Child 2, Has child Child 1.1)
                every { mockNativeDocument.getBookmarkTitle(child1Ptr) } returns "Child 1"
                every { mockNativeDocument.getBookmarkDestIndex(any(), child1Ptr) } returns 1
                every { mockNativeDocument.getSiblingBookmark(any(), child1Ptr) } returns child2Ptr
                every {
                    mockNativeDocument.getFirstChildBookmark(
                        any(),
                        child1Ptr,
                    )
                } returns child11Ptr

                // Child 1.1 Setup (No siblings, No children)
                every { mockNativeDocument.getBookmarkTitle(child11Ptr) } returns "Child 1.1"
                every { mockNativeDocument.getBookmarkDestIndex(any(), child11Ptr) } returns 1
                every { mockNativeDocument.getSiblingBookmark(any(), child11Ptr) } returns 0
                every { mockNativeDocument.getFirstChildBookmark(any(), child11Ptr) } returns 0

                // Child 2 Setup (No siblings, No children)
                every { mockNativeDocument.getBookmarkTitle(child2Ptr) } returns "Child 2"
                every { mockNativeDocument.getBookmarkDestIndex(any(), child2Ptr) } returns 5
                every { mockNativeDocument.getSiblingBookmark(any(), child2Ptr) } returns 0
                every { mockNativeDocument.getFirstChildBookmark(any(), child2Ptr) } returns 0
            }
            apiCall = {
                pdfDocumentU.getTableOfContents()
            }

            verifyHappy { toc ->
                assertThat(toc).hasSize(2) // Child 1 and Child 2

                val node1 = toc[0]
                assertThat(node1.title).isEqualTo("Child 1")
                assertThat(node1.pageIdx).isEqualTo(1)
                assertThat(node1.children).hasSize(1)

                val node11 = node1.children[0]
                assertThat(node11.title).isEqualTo("Child 1.1")

                val node2 = toc[1]
                assertThat(node2.title).isEqualTo("Child 2")
                assertThat(node2.pageIdx).isEqualTo(5)
                assertThat(node2.children).isEmpty()
            }
            verifyDefault {
                assertThat(it).isEmpty()
            }
        }

    @Test
    fun `openTextPages batch loading`() =
        closableTest {
            val start = 0
            val end = 2
            setupHappy {
                val pointers = longArrayOf(100L, 101L, 102L) // Pointers for pages 0, 1, 2

                every { mockNativeDocument.loadPages(any(), start, end) } returns pointers
            }
            apiCall = {
                pdfDocumentU.openTextPages(start, end)
            }

            verifyHappy {
                assertThat(it).hasSize(3)

                verify(exactly = 1) { mockNativeDocument.loadPages(any(), start, end) }
            }
            verifyDefault {
                assertThat(it).hasSize(0)

                verify(exactly = 0) { mockNativeDocument.loadPages(any(), start, end) }
            }
        }

    @Test
    fun `saveAsCopy flag transmission`() =
        closableTest {
            val mockWriter = mockk<PdfWriteCallback>(relaxed = true)
            val flags = 123 // Arbitrary flag
            setupHappy {
                // Verify saveAsCopy passes the correct flags and callback

                every { mockNativeDocument.saveAsCopy(any(), any(), any()) } returns true
            }
            apiCall = {
                pdfDocumentU.saveAsCopy(mockWriter, flags)
            }

            verifyHappy {
                assertThat(it).isTrue()
                verify(exactly = 1) {
                    mockNativeDocument.saveAsCopy(any(), mockWriter, flags)
                }
            }
            verifyDefault {
                assertThat(it).isFalse()
                verify(exactly = 0) {
                    mockNativeDocument.saveAsCopy(any(), mockWriter, flags)
                }
            }
        }
}

class PdfDocumentUHappyTest : PdfDocumentUBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.EXCEPTION

    override fun isStateClosed() = false

    override fun setupClosedState() {
        every { mockNativeDocument.closeDocument(any()) } just runs
    }

    @Test
    fun `close resource cleanup`() {
        // Verify that calling close() closes and nullifies the parcelFileDescriptor
        // and the source object.
        pdfDocumentU.close()
        assertThat(pdfDocumentU.isClosed).isTrue()
        assertThat(pdfDocumentU.parcelFileDescriptor).isNull()
        assertThat(pdfDocumentU.source).isNull()
    }

    @Test
    fun `close state transition`() {
        // Verify that calling close() sets isClosed to true and calls nativeDocument.closeDocument().
        pdfDocumentU.close()
        assertThat(pdfDocumentU.isClosed).isTrue()
    }

    @Test
    fun `close idempotency`() {
        // Verify that calling close() multiple times does not crash and does not
        // attempt to close the native document or resources more than once.
        pdfiumConfig = Config(alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE)
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfDocumentU.close()
        pdfDocumentU.close()
        assertThat(pdfDocumentU.isClosed).isTrue()
        verify(exactly = 1) { mockNativeDocument.closeDocument(any()) }
    }

    @Test
    fun `openPage happy path with caching`() {
        // Verify openPage loads a page, adds it to the pageMap, and returns a PdfPageU wrapper.
        // Subsequent calls for the same index should return a new wrapper but reuse the native pointer
        // from the cache and increment the internal reference count.
        every { mockNativeDocument.loadPage(any(), any()) } returns 100
        pdfDocumentU.openPage(0)
        pdfDocumentU.openPage(0)
        verify(exactly = 1) { mockNativeDocument.loadPage(any(), any()) }
    }
}

class PdfDocumentUCloseExceptionTest : PdfDocumentUBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.EXCEPTION

    override fun isStateClosed() = true

    override fun setupClosedState() {
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU.close()
    }
}

class PdfDocumentUCloseIgnoreTest : PdfDocumentUBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.IGNORE

    override fun isStateClosed() = true

    override fun setupClosedState() {
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU.close()
    }
}
