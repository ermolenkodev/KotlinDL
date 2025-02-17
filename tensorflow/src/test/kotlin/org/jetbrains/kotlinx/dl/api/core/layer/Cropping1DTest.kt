/*
 * Copyright 2020-2022 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package org.jetbrains.kotlinx.dl.api.core.layer

import org.jetbrains.kotlinx.dl.api.core.layer.reshaping.Cropping1D
import org.jetbrains.kotlinx.dl.api.core.shape.shape
import org.jetbrains.kotlinx.dl.api.core.shape.toLongArray
import org.junit.jupiter.api.Test

internal class Cropping1DTest : LayerTest() {
    private val input = arrayOf(
        arrayOf(
            floatArrayOf(1.0f, 2.0f, 3.0f),
            floatArrayOf(4.0f, 5.0f, 6.0f),
            floatArrayOf(7.0f, 8.0f, 9.0f),
            floatArrayOf(10.0f, 11.0f, 12.0f),
        ),
        arrayOf(
            floatArrayOf(-1.0f, -2.0f, -3.0f),
            floatArrayOf(-4.0f, -5.0f, -6.0f),
            floatArrayOf(-7.0f, -8.0f, -9.0f),
            floatArrayOf(-10.0f, -11.0f, -12.0f),
        )
    )

    private val inputShape = input.shape.toLongArray()

    @Test
    fun testWithCroppingSize() {
        val layer = Cropping1D(cropping = intArrayOf(1, 2))
        val expected = arrayOf(
            arrayOf(
                floatArrayOf(4.0f, 5.0f, 6.0f),
            ),
            arrayOf(
                floatArrayOf(-4.0f, -5.0f, -6.0f),
            )
        )
        assertLayerOutputIsCorrect(layer, input, expected)
        val expectedShape = longArrayOf(inputShape[0], inputShape[1] - 1 - 2, inputShape[2])
        assertLayerComputedOutputShape(layer, expectedShape)
    }
}
