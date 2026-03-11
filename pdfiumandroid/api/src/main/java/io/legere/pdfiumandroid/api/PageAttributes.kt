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
import io.legere.geokt.KtImmutableMatrix
import io.legere.geokt.KtImmutableRectF

/**
 * PageAttributes contains various attributes of a PDF page.
 */
@Keep
data class PageAttributes(
    val page: Int,
    val pageWidth: Int,
    val pageHeight: Int,
    val pageRotation: Int,
    val rect: KtImmutableRectF,
    val mediaBox: KtImmutableRectF,
    val cropBox: KtImmutableRectF,
    val bleedBox: KtImmutableRectF,
    val trimBox: KtImmutableRectF,
    val artBox: KtImmutableRectF,
    val boundingBox: KtImmutableRectF,
    val links: List<Link>,
    val pageMatrix: KtImmutableMatrix,
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
                rect = KtImmutableRectF(0f, 0f, 0f, 0f),
                mediaBox = KtImmutableRectF(0f, 0f, 0f, 0f),
                cropBox = KtImmutableRectF(0f, 0f, 0f, 0f),
                bleedBox = KtImmutableRectF(0f, 0f, 0f, 0f),
                trimBox = KtImmutableRectF(0f, 0f, 0f, 0f),
                artBox = KtImmutableRectF(0f, 0f, 0f, 0f),
                boundingBox = KtImmutableRectF(0f, 0f, 0f, 0f),
                links = emptyList(),
                pageMatrix = KtImmutableMatrix(),
            )
    }
}
