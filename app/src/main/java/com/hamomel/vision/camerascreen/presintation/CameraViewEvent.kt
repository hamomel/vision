package com.hamomel.vision.camerascreen.presintation

import android.graphics.Bitmap

sealed interface CameraViewEvent

object ShowNeedPermissionsDialog : CameraViewEvent

data class ShowSearchScreen(
    val bitmap: Bitmap
) : CameraViewEvent
