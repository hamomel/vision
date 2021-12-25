package com.hamomel.vision.camerascreen.detection

import android.graphics.Bitmap
import android.media.Image
import android.util.Size
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.internal.ImageConvertUtils
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetector
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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

    // it would be more convenient to join image size with detected objects into dedicated class,
    // but we would need to create a new object each recognition,
    // and it would create unwanted load on the garbage collector
    private val _imageSize = MutableStateFlow(Size(0, 0))
    val imageSize: Flow<Size> get() = _imageSize

    private var lastImageCallback: ((Bitmap, List<DetectedObject>) -> Unit)? = null

    fun process(mediaImage: Image, rotation: Int) {
        val image = InputImage.fromMediaImage(mediaImage, rotation)
        val task = detector.process(image)
        val detectedObjects = Tasks.await(task)

        handleCallback(image, detectedObjects)

        val currentSize = _imageSize.value
        if (image.width != currentSize.width || image.height != currentSize.height) {
            _imageSize.value = if (rotation == 90 || rotation == 180) {
                Size(image.height, image.width)
            } else {
                Size(image.width, image.height)
            }
        }
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
