package com.hamomel.vision.searchresults.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hamomel.vision.R
import com.hamomel.vision.databinding.FragmentVisualSearchBinding
import com.hamomel.vision.utils.SpaceItemDecoration
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * @author Роман Зотов on 12.12.2021
 */
class VisualSearchResultsFragment : Fragment(R.layout.fragment_visual_search) {

    // Usually it is bad practice passing Fragment's parameters directly,
    // because they are not restored on fragment recreation. But here we have to pass
    // Bitmap object which might be too large to put it in Bundle. I decided to pass it directly
    // into a field of the fragment and just close the fragment if bitmap is null
    private var bitmap: Bitmap? = null

    private lateinit var binding: FragmentVisualSearchBinding
    private val viewModel by viewModel<VisualSearchViewModel> { parametersOf(bitmap) }

    private var adapter: VisualSearchAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (bitmap == null) {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVisualSearchBinding.bind(view)

        adapter = VisualSearchAdapter { viewModel.onItemClick(it) }
        binding.resultsRecyclerView.adapter = adapter
        val itemDecoration = SpaceItemDecoration(
            requireContext().resources.getDimensionPixelSize(R.dimen.visual_search_result_item_space)
        )
        binding.resultsRecyclerView.addItemDecoration(itemDecoration)

        binding.retryButton.setOnClickListener { viewModel.onRetryClick() }
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    renderState(state)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                subscribeToEvents()
            }
        }

        binding.preview.setImageBitmap(bitmap)
    }

    private suspend fun subscribeToEvents() {
        viewModel.viewEvents.collect { event ->
            when (event) {
                is OpenResultScreen -> openResultViewScreen(event.pageUrl)
            }
        }
    }

    private fun renderState(state: ResultsViewState) = with(binding) {
        resultsRecyclerView.isVisible = state is Success
        errorLayout.isVisible = state is Error
        progressbar.isVisible = state is Loading

        if (state is Success) {
            adapter?.setData(state.items)
        }
    }

    private fun openResultViewScreen(pageUrl: String) {
        val uri = Uri.parse(pageUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        requireActivity().startActivity(intent)
    }

    companion object {
        fun create(image: Bitmap): VisualSearchResultsFragment =
            VisualSearchResultsFragment().apply {
                bitmap = image
            }
    }
}
