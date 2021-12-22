package com.hamomel.vision.camerascreen.detection

import android.content.Context
import androidx.annotation.MainThread
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * @author Роман Зотов on 12.12.2021
 */
class CameraController(
    private val context: Context,
    private val mainExecutor: Executor,
    private val model: DetectionModel
) {

    @MainThread
    fun startStreaming(lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(
            createCameraBindRunnable(cameraProviderFuture, lifecycleOwner, surfaceProvider),
            mainExecutor
        )
    }

    private fun createCameraBindRunnable(
        cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
        lifecycleOwner: LifecycleOwner,
        previewSurfaceProvider: Preview.SurfaceProvider
    ): Runnable {
        return Runnable {
            val cameraProvider = cameraProviderFuture.get()

            cameraProvider?.unbindAll()
            bindUsecases(lifecycleOwner, cameraProvider, previewSurfaceProvider)
        }
    }

    private fun bindUsecases(
        lifecycleOwner: LifecycleOwner,
        cameraProvider: ProcessCameraProvider,
        previewSurfaceProvider: Preview.SurfaceProvider) {
        val preview = Preview.Builder()
            .build()
            .apply {
                setSurfaceProvider(previewSurfaceProvider)
            }

        val imageAnalysis = createImageAnalysis()

        try {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (e: IllegalStateException) {
            Timber.e(e, "couldn't bind use cases")
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "couldn't bind use cases")
        }
    }

    @Suppress("UnsafeOptInUsageError")
    private fun createImageAnalysis(): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val executor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            imageProxy.use {
                imageProxy.image?.let { image ->
                    model.process(image, imageProxy.imageInfo.rotationDegrees)
                }
            }
        }

        return imageAnalysis
    }
}
