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

/**
 * Represents a bookmark (table of contents entry) within a PDF document.
 *
 * @property children A mutable list of child bookmarks, allowing for a hierarchical structure.
 * @property title The title of the bookmark.
 * @property pageIdx The 0-based page index that this bookmark points to.
 * @property mNativePtr The native pointer to the underlying FPDF_BOOKMARK object.
 */
data class Bookmark(
    val children: MutableList<Bookmark> = ArrayList(),
    var title: String? = null,
    var pageIdx: Long = 0,
    var mNativePtr: Long = 0,
)
