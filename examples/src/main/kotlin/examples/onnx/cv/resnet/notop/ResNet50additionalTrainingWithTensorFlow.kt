/*
 * Copyright 2020-2022 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package examples.onnx.cv.resnet.notop

import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.HeNormal
import org.jetbrains.kotlinx.dl.api.core.initializer.Zeros
import org.jetbrains.kotlinx.dl.api.core.layer.core.Dense
import org.jetbrains.kotlinx.dl.api.core.layer.core.Input
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.GlobalAvgPool2D
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import org.jetbrains.kotlinx.dl.dataset.OnFlyImageDataset
import org.jetbrains.kotlinx.dl.dataset.embedded.dogsCatsDatasetPath
import org.jetbrains.kotlinx.dl.dataset.embedded.dogsCatsSmallDatasetPath
import org.jetbrains.kotlinx.dl.dataset.generator.FromFolders
import org.jetbrains.kotlinx.dl.onnx.dataset.preprocessor.onnx
import org.jetbrains.kotlinx.dl.onnx.inference.ONNXModelHub
import org.jetbrains.kotlinx.dl.onnx.inference.ONNXModels
import org.jetbrains.kotlinx.dl.onnx.inference.ONNXModels.CVnoTop.Companion.createPreprocessing
import java.io.File

private const val EPOCHS = 3
private const val TRAINING_BATCH_SIZE = 16
private const val TEST_BATCH_SIZE = 32
private const val NUM_CLASSES = 2
private const val TRAIN_TEST_SPLIT_RATIO = 0.8

/**
 * This is a simple model based on Dense layers only.
 */
private val topModel = Sequential.of(
    Input(2, 2, 2048),
    GlobalAvgPool2D(),
    Dense(NUM_CLASSES, Activations.Linear, kernelInitializer = HeNormal(12L), biasInitializer = Zeros())
)

/**
 * This examples demonstrates the transfer learning concept on ResNet'50 model:
 * - Model configuration, model weights and labels are obtained from [ONNXModelHub].
 * - All layers, excluding the last [Dense], are added to the new Neural Network, its weights are frozen.
 * - ONNX frozen model is used as a preprocessing stage via `onnx` stage of the Image Preprocessing DSL.
 * - New Dense layers are added and initialized via defined initializers.
 * - Model is re-trained on [dogsCatsDatasetPath] dataset.
 *
 *
 * We use the preprocessing DSL to describe the dataset generation pipeline.
 * We demonstrate the workflow on the subset of Kaggle Cats vs Dogs binary classification dataset.
 */
fun resnet50additionalTraining() {
    val modelHub = ONNXModelHub(
        cacheDirectory = File("cache/pretrainedModels")
    )
    val modelType = ONNXModels.CVnoTop.ResNet50Custom

    modelHub.loadModel(modelType).use { model ->
        println(model)
        model.reshape(64, 64, 3)

        val preprocessing = modelType.createPreprocessing(model).onnx { onnxModel = model }

        val dogsVsCatsDatasetPath = dogsCatsSmallDatasetPath()
        val dataset = OnFlyImageDataset.create(
            File(dogsVsCatsDatasetPath),
            FromFolders(mapping = mapOf("cat" to 0, "dog" to 1)),
            preprocessing
        ).shuffle()
        val (train, test) = dataset.split(TRAIN_TEST_SPLIT_RATIO)

        topModel.use {
            topModel.compile(
                optimizer = Adam(),
                loss = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
                metric = Metrics.ACCURACY
            )

            topModel.fit(dataset = train, epochs = EPOCHS, batchSize = TRAINING_BATCH_SIZE)

            val accuracy = topModel.evaluate(dataset = test, batchSize = TEST_BATCH_SIZE).metrics[Metrics.ACCURACY]

            println("Accuracy: $accuracy")
        }
    }
}

/** */
fun main(): Unit = resnet50additionalTraining()


