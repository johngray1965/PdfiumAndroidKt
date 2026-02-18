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

package io.legere.pdfiumandroid.api

import androidx.annotation.Keep
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.PdfRectF

/**
 * PageAttributes contains various attributes of a PDF page.
 */
@Keep
data class PageAttributes(
    val page: Int,
    val pageWidth: Int,
    val pageHeight: Int,
    val pageRotation: Int,
    val rect: PdfRectF,
    val mediaBox: PdfRectF,
    val cropBox: PdfRectF,
    val bleedBox: PdfRectF,
    val trimBox: PdfRectF,
    val artBox: PdfRectF,
    val boundingBox: PdfRectF,
    val links: List<Link>,
    val pageMatrix: PdfMatrix,
) {
    companion object {
        /**
         * An empty PageInfo object.
         */
        val EMPTY =
            PageAttributes(
                page = -1,
                pageWidth = 0,
                pageHeight = 0,
                pageRotation = 0,
                rect = PdfRectF(0f, 0f, 0f, 0f),
                mediaBox = PdfRectF(0f, 0f, 0f, 0f),
                cropBox = PdfRectF(0f, 0f, 0f, 0f),
                bleedBox = PdfRectF(0f, 0f, 0f, 0f),
                trimBox = PdfRectF(0f, 0f, 0f, 0f),
                artBox = PdfRectF(0f, 0f, 0f, 0f),
                boundingBox = PdfRectF(0f, 0f, 0f, 0f),
                links = emptyList(),
                pageMatrix = PdfMatrix(),
            )
    }
}
