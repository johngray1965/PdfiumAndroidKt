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

package io.legere.pdfiumandroid.api.types

interface RectInterface<VT : Number, T, B> {
    val left: VT
    val top: VT
    val right: VT
    val bottom: VT

    fun height(): VT

    fun width(): VT

    fun centerX(): VT

    fun centerY(): VT

    fun contains(
        left: VT,
        top: VT,
        right: VT,
        bottom: VT,
    ): Boolean

    fun contains(
        x: VT,
        y: VT,
    ): Boolean

    fun contains(rect: B): Boolean

    fun isEmpty(): Boolean

    fun intersects(other: B): Boolean

    fun intersects(
        left: VT,
        top: VT,
        right: VT,
        bottom: VT,
    ): Boolean
}

interface ImmutableRectInterface<VT : Number, T, B> {
    fun inset(
        left: VT,
        top: VT,
        right: VT,
        bottom: VT,
    ): T

    fun inset(
        dx: VT,
        dy: VT,
    ): T

    fun intersect(other: B): T

    fun intersect(
        left: VT,
        top: VT,
        right: VT,
        bottom: VT,
    ): T

    fun offset(
        dx: VT,
        dy: VT,
    ): T

    fun offsetTo(
        newLeft: VT,
        newTop: VT,
    ): T

    fun union(other: B): T

    fun union(
        left: VT,
        top: VT,
        right: VT,
        bottom: VT,
    ): T

    fun union(
        x: VT,
        y: VT,
    ): T

    fun sort(): T
}

interface MutableRectInterface<VT : Number, T, B> {
    fun inset(
        left: VT,
        top: VT,
        right: VT,
        bottom: VT,
    )

    fun inset(
        dx: VT,
        dy: VT,
    )

    fun intersect(other: B)

    fun intersect(
        left: VT,
        top: VT,
        right: VT,
        bottom: VT,
    )

    fun offset(
        dx: VT,
        dy: VT,
    )

    fun offsetTo(
        newLeft: VT,
        newTop: VT,
    )

    fun union(other: B)

    fun union(
        left: VT,
        top: VT,
        right: VT,
        bottom: VT,
    )

    fun union(
        x: VT,
        y: VT,
    )

    fun sort()
}
