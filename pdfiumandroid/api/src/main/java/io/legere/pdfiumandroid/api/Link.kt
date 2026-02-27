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

/**
 * Represents a link (e.g., internal page link or URI link) found on a PDF page.
 *
 * @property bounds The bounding rectangle of the link on the page.
 * @property destPageIdx The 0-based destination page index if this is an internal link,
 * or `null` if it's a URI link.
 * @property uri The URI string if this is a web link, or `null` if it's an internal page link.
 */
class Link(
    val bounds: RectF,
    val destPageIdx: Int?,
    val uri: String?,
)
