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
package io.legere.pdfiumandroid.util

import io.legere.pdfiumandroid.api.Config as NewConfig
import io.legere.pdfiumandroid.api.Size as NewSize

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.Size",
    ReplaceWith("io.legere.pdfiumandroid.api.Size", "io.legere.pdfiumandroid.api.Size"),
)
typealias Size = NewSize

@Deprecated(
    "Moved to io.legere.pdfiumandroid.api.Config",
    ReplaceWith("io.legere.pdfiumandroid.api.Config", "io.legere.pdfiumandroid.api.Config"),
)
typealias Config = NewConfig
