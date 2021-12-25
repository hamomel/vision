package com.hamomel.vision.camerascreen.presintation

import android.Manifest
import android.graphics.Bitmap
import android.util.Size
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraViewModel(
    private val detectionModel: DetectionModel,
    private val cameraController: CameraController,
    private val permissionsChecker: PermissionChecker,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _detectedObjects = MutableStateFlow(emptyList<DetectedObject>())
    val detectedObjects: Flow<List<DetectedObject>> get() = _detectedObjects

    // I'd prefer to pass state to view in a single flow but in this case state would be recreated
    // on each detected objects update and load garbage collector, as detection occurs tens times per second
    private val _imageSize = MutableStateFlow<Size>(Size(0, 0))
    val imageSize: Flow<Size> get() = _imageSize

    private val _viewEvents = MutableSharedFlow<CameraViewEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val viewEvents: Flow<CameraViewEvent> get() = _viewEvents

    init {
        detectionModel.detectedObjects.onEach {
            _detectedObjects.emit(it)
        }.launchIn(viewModelScope)

        detectionModel.imageSize.onEach {
            _imageSize.emit(it)
        }.launchIn(viewModelScope)
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

    fun onObjectSelected(obj: DetectedObject) {
        viewModelScope.launch {
            val image = detectionModel.getLastImage()
            // DetectedObject's tracingId might be null if object detector in single object mode
            // than there is only one object in the list
            val boundingBox = image.detectedObjects
                .firstOrNull {
                    obj.trackingId == null || it.trackingId == obj.trackingId
                }
                ?.boundingBox
                ?: return@launch

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