package com.hamomel.vision.camerascreen.detection

import android.graphics.Bitmap
import android.media.Image
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.internal.ImageConvertUtils
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetector
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @author Роман Зотов on 12.12.2021
 */
class DetectionModel(
    private val detector: ObjectDetector,
    private val imageConverter: ImageConvertUtils = ImageConvertUtils.getInstance()
) {

    private val _detectedObjects = MutableSharedFlow<List<DetectedObject>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val detectedObjects: Flow<List<DetectedObject>> get() = _detectedObjects

    private var lastImageCallback: ((Bitmap, List<DetectedObject>) -> Unit)? = null

    fun process(mediaImage: Image, rotation: Int) {
        val image = InputImage.fromMediaImage(mediaImage, rotation)
        val task = detector.process(image)
        val detectedObjects = Tasks.await(task)

        handleCallback(image, detectedObjects)

        _detectedObjects.tryEmit(detectedObjects)
    }

    private fun handleCallback(image: InputImage, detectedObjects: List<DetectedObject>) {
        if (lastImageCallback == null) return
        val bitmap = imageConverter.convertToUpRightBitmap(image)

        lastImageCallback?.invoke(bitmap, detectedObjects)
    }

    suspend fun getLastImage(): ImageWithDetectedObjects = suspendCancellableCoroutine { continuation ->
        lastImageCallback = { bitmap, detectedObjects ->
            val result = ImageWithDetectedObjects(bitmap, detectedObjects)
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
    }
}
