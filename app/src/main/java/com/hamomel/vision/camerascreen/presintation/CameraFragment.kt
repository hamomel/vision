package com.hamomel.vision.camerascreen.presintation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hamomel.vision.R
import com.hamomel.vision.databinding.FragmentCameraBinding
import com.hamomel.vision.permissions.PermissionChecker
import com.hamomel.vision.searchresults.presentation.VisualSearchResultsFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CameraFragment : Fragment(R.layout.fragment_camera), PermissionDialogActionsHandler {

    private lateinit var binding: FragmentCameraBinding
    private val viewModel: CameraViewModel by viewModel()
    private val permissionChecker: PermissionChecker by inject()
    private var eventsJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCameraBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detectedObjects.collect { state ->
                    binding.captureButton.isActivated = state.isNotEmpty()
                    binding.overlayView.objects = state
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.detectedObjects.collect { objects ->
                    binding.captureButton.isActivated = objects.isNotEmpty()
                    binding.overlayView.objects = objects
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.imageSize.collect { size ->
                    binding.overlayView.updateImageSize(size)
                }
            }
        }

        binding.captureButton.setOnClickListener {
            val objectInCenter = binding.overlayView.getMostCenteredObject()
            objectInCenter?.let {
                viewModel.onObjectSelected(it)
            }
        }
        binding.overlayView.setOnSpotTouchListener { obj ->
            viewModel.onObjectSelected(obj)
        }
    }

    override fun onStart() {
        super.onStart()
        // subscribe manually to make sure that events received when interaction with ViewModel started
        subscribeToViewEvents()
        permissionChecker.attach(requireActivity())
        viewModel.onStart(this, binding.previewView.surfaceProvider)
    }

    override fun onStop() {
        super.onStop()
        permissionChecker.detach()
        eventsJob?.cancel()
    }

    private fun subscribeToViewEvents() {
        eventsJob = lifecycleScope.launch {
            viewModel.viewEvents.collect { event ->
                when (event) {
                    ShowNeedPermissionsDialog -> showNeedPermissionDialog()
                    is ShowSearchScreen -> showSearchFragment(event)
                }
            }
        }
    }

    private fun showSearchFragment(event: ShowSearchScreen) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, VisualSearchResultsFragment.create(event.bitmap))
            .addToBackStack(null)
            .commit()
    }

    private fun showNeedPermissionDialog() {
        NeedPermissionsDialogFragment().show(
            childFragmentManager,
            NeedPermissionsDialogFragment::class.java.name
        )
    }

    override fun onPositiveClick() {
        openAppSettings()
    }

    private fun openAppSettings() {
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)

        requireActivity().startActivity(intent)
    }

    override fun onNegativeClick() {
        // TODO: 18.12.2021 do it in a router
        requireActivity().finish()
    }

    companion object {
        fun create() = CameraFragment()
    }
}