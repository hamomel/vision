package com.hamomel.vision.searchresults.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamomel.vision.searchresults.data.VisualSearchNetworkDataSource
import com.hamomel.vision.searchresults.data.model.VisualSearchItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * @author Роман Зотов on 12.12.2021
 */

private const val MIN_PHOTO_SIZE = 350

class VisualSearchViewModel(
    private val image: Bitmap?,
    private val dataSource: VisualSearchNetworkDataSource
) : ViewModel() {

    private val _viewState = MutableStateFlow<ResultsViewState>(Loading)
    val viewState: Flow<ResultsViewState> get() = _viewState

    private val _viewEvents = MutableSharedFlow<ResultsViewEvent>()
    val viewEvents: Flow<ResultsViewEvent> get() = _viewEvents

    init {
        search()
    }

    fun onRetryClick() {
        search()
    }

    private fun search() {
        if (image == null) {
            _viewState.value = Error
            return
        }
        viewModelScope.launch {
            try {
                // often api returns pictures from avatars etc. so let's filter out such photos
                val items = dataSource.search(image)
                    .filter { it.width > MIN_PHOTO_SIZE && it.height > MIN_PHOTO_SIZE }
                _viewState.value = Success(items)
            } catch (e: Exception) {
                Timber.e(e, "failed to get recognition results")
                _viewState.value = Error
            }
        }
    }

    fun onItemClick(item: VisualSearchItem) {
        viewModelScope.launch {
            _viewEvents.emit(OpenResultScreen(item.hostPageUrl))
        }
    }
}
