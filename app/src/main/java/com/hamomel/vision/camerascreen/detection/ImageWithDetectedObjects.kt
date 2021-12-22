package com.hamomel.vision.camerascreen.detection

import android.graphics.Bitmap
import com.google.mlkit.vision.objects.DetectedObject

data class ImageWithDetectedObjects(
    val image: Bitmap,
    val detectedObjects: List<DetectedObject>
)