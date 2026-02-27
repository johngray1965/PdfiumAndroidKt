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

import android.graphics.RectF
import androidx.annotation.Keep

/**
 * PageAttributes contains various attributes of a PDF page.
 */
@Keep
data class PageAttributes(
    val page: Int,
    val pageWidth: Int,
    val pageHeight: Int,
    val pageRotation: Int,
    val rect: RectF,
    val mediaBox: RectF,
    val cropBox: RectF,
    val bleedBox: RectF,
    val trimBox: RectF,
    val artBox: RectF,
    val boundingBox: RectF,
    val links: List<Link>,
    val pageMatrix: ImmutableMatrix,
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
                rect = RectF(),
                mediaBox = RectF(),
                cropBox = RectF(),
                bleedBox = RectF(),
                trimBox = RectF(),
                artBox = RectF(),
                boundingBox = RectF(),
                links = emptyList(),
                pageMatrix = ImmutableMatrix(),
            )
    }
}
