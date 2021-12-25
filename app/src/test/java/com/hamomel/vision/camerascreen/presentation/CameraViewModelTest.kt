package com.hamomel.vision.camerascreen.presentation

import android.Manifest
import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.objects.DetectedObject
import com.hamomel.vision.TestCoroutineRule
import com.hamomel.vision.camerascreen.detection.CameraController
import com.hamomel.vision.camerascreen.detection.DetectionModel
import com.hamomel.vision.camerascreen.detection.ImageWithDetectedObjects
import com.hamomel.vision.camerascreen.presintation.CameraViewModel
import com.hamomel.vision.camerascreen.presintation.ShowNeedPermissionsDialog
import com.hamomel.vision.camerascreen.presintation.ShowSearchScreen
import com.hamomel.vision.permissions.Denied
import com.hamomel.vision.permissions.Granted
import com.hamomel.vision.permissions.PermissionChecker
import com.hamomel.vision.permissions.ShouldShowRationale
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * @author Роман Зотов on 22.12.2021
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val detectionModel: DetectionModel = mockk()
    private val cameraController: CameraController = mockk()
    private val permissionsChecker: PermissionChecker = mockk()

    private val testDetectedObjects = List(4) { mockk<DetectedObject>(relaxed = true) }

    private lateinit var viewModel: CameraViewModel

    @Before
    fun setup() {
        every { detectionModel.detectedObjects } returns flowOf(testDetectedObjects)
        every { detectionModel.imageSize } returns flowOf(Size(100, 100))

        viewModel = CameraViewModel(
            detectionModel = detectionModel,
            cameraController = cameraController,
            permissionsChecker = permissionsChecker,
            defaultDispatcher = testCoroutineRule.testDispatcher
        )
    }

    @Test
    fun `should expose same DetectedObjects that get from detectionModel`() = runTest {
        runCurrent()
        val viewState = viewModel.detectedObjects.first()

        assertEquals(testDetectedObjects, viewState)
    }

    @Test
    fun `should check permission when onStart called`() = runTest {
        val lifecycleOwner = mockk<LifecycleOwner>()
        val surfaceProvider = mockk<Preview.SurfaceProvider>()

        viewModel.onStart(lifecycleOwner, surfaceProvider)
        runCurrent()

        coVerify(exactly = 1) { permissionsChecker.checkAndRequestIfNeeded(Manifest.permission.CAMERA) }
    }

    @Test
    fun `should start recognition if permission is granted`() = runTest {
        val lifecycleOwner = mockk<LifecycleOwner>()
        val surfaceProvider = mockk<Preview.SurfaceProvider>()

        coEvery { permissionsChecker.checkAndRequestIfNeeded(any()) } returns Granted

        viewModel.onStart(lifecycleOwner, surfaceProvider)
        runCurrent()

        verify(exactly = 1) { cameraController.startStreaming(lifecycleOwner, surfaceProvider) }
    }

    @Test
    fun `should send SnowNeedPermission if permission is Denied`() = runTest {
        val lifecycleOwner = mockk<LifecycleOwner>()
        val surfaceProvider = mockk<Preview.SurfaceProvider>()

        coEvery { permissionsChecker.checkAndRequestIfNeeded(any()) } returns Denied

        val eventDeferred = async { viewModel.viewEvents.first() }

        viewModel.onStart(lifecycleOwner, surfaceProvider)
        runCurrent()

        val event = eventDeferred.await()

        assertEquals(ShowNeedPermissionsDialog, event)
    }

    @Test
    fun `should send SnowNeedPermission if permission is ShouldShowRationale`() = runTest {
        val lifecycleOwner = mockk<LifecycleOwner>()
        val surfaceProvider = mockk<Preview.SurfaceProvider>()

        coEvery { permissionsChecker.checkAndRequestIfNeeded(any()) } returns ShouldShowRationale

        val eventDeferred = async { viewModel.viewEvents.first() }

        viewModel.onStart(lifecycleOwner, surfaceProvider)
        runCurrent()

        val event = eventDeferred.await()

        assertEquals(ShowNeedPermissionsDialog, event)
    }

    @Test
    fun `should request image from detection model when capture button pressed`() = runTest {
        val testInputBitmap = mockk<Bitmap>()

        val lastImage = ImageWithDetectedObjects(
            image = testInputBitmap,
            detectedObjects = testDetectedObjects
        )

        coEvery { detectionModel.getLastImage() } returns lastImage

        viewModel.onObjectSelected(testDetectedObjects[0])
        runCurrent()

        coVerify(exactly = 1) { detectionModel.getLastImage() }
    }

    @Test
    fun `should send ShowSearchScreen when capture button pressed`() = runTest {
        val testInputBitmap = mockk<Bitmap>()
        val testOutputBitmap = mockk<Bitmap>()

        val lastImage = ImageWithDetectedObjects(
            image = testInputBitmap,
            detectedObjects = testDetectedObjects
        )

        coEvery { detectionModel.getLastImage() } returns lastImage
        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(any<Bitmap>(), any(), any(), any(), any())
        } returns testOutputBitmap

        val eventDeferred = async { viewModel.viewEvents.first() }
        runCurrent()

        viewModel.onObjectSelected(testDetectedObjects[0])
        runCurrent()

        val event = eventDeferred.await()

        assertEquals(testOutputBitmap, (event as ShowSearchScreen).bitmap)
        unmockkStatic(Bitmap::class)
    }
}