package com.hamomel.vision.camerascreen.di

import androidx.core.content.ContextCompat
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.hamomel.vision.camerascreen.detection.CameraController
import com.hamomel.vision.camerascreen.detection.DetectionModel
import com.hamomel.vision.camerascreen.presintation.CameraViewModel
import com.hamomel.vision.permissions.PermissionChecker
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val cameraScreenModule = module {
    val detectorOptions = ObjectDetectorOptions.Builder().build()

    single {
        DetectionModel(detector = ObjectDetection.getClient(detectorOptions))
    }
    single {
        CameraController(
            context = get(),
            mainExecutor = ContextCompat.getMainExecutor(get()),
            model = get()
        )
    }
    single { PermissionChecker() }

    viewModel {
        CameraViewModel(
            detectionModel = get(),
            cameraController = get(),
            permissionsChecker = get()
        )
    }
}