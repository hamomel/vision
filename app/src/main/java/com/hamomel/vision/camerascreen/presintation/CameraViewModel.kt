package com.hamomel.vision.camerascreen.presintation

import android.Manifest
import android.graphics.Bitmap
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.objects.DetectedObject
import com.hamomel.vision.camerascreen.detection.CameraController
import com.hamomel.vision.camerascreen.detection.DetectionModel
import com.hamomel.vision.permissions.Granted
import com.hamomel.vision.permissions.PermissionChecker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraViewModel(
    private val detectionModel: DetectionModel,
    private val cameraController: CameraController,
    private val permissionsChecker: PermissionChecker,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _viewState = MutableStateFlow(emptyList<DetectedObject>())
    val viewState: Flow<List<DetectedObject>> get() = _viewState

    private val _viewEvents = MutableSharedFlow<CameraViewEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val viewEvents: Flow<CameraViewEvent> get() = _viewEvents

    init {
        viewModelScope.launch {
            detectionModel.detectedObjects.collect {
                _viewState.emit(it)
            }
        }
    }

    fun onStart(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        viewModelScope.launch {
            val permissionCheckResult =
                permissionsChecker.checkAndRequestIfNeeded(Manifest.permission.CAMERA)

            if (permissionCheckResult == Granted) {
                startRecognition(lifecycleOwner, surfaceProvider)
            } else {
                _viewEvents.emit(ShowNeedPermissionsDialog)
            }
        }
    }

    private fun startRecognition(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        cameraController.startStreaming(lifecycleOwner, surfaceProvider)
    }

    fun onButtonClick() {
        viewModelScope.launch {
            val image = detectionModel.getLastImage()
            val boundingBox = image.detectedObjects.firstOrNull()?.boundingBox ?: return@launch

            withContext(defaultDispatcher) {
                val width = boundingBox.right - boundingBox.left
                val height = boundingBox.bottom - boundingBox.top
                val objectBitmap = Bitmap.createBitmap(image.image, boundingBox.left, boundingBox.top, width, height)

                _viewEvents.emit(
                    ShowSearchScreen(objectBitmap)
                )
            }
        }
    }
}