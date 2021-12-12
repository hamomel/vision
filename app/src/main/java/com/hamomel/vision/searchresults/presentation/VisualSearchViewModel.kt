package com.hamomel.vision.searchresults.presentation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamomel.vision.searchresults.data.VisualSearchNetworkDataSource
import com.hamomel.vision.searchresults.data.model.VisualSearchItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * @author Роман Зотов on 12.12.2021
 */
class VisualSearchViewModel(
    private val image: Bitmap?,
    private val dataSource: VisualSearchNetworkDataSource
) : ViewModel() {

    private val _viewState = MutableStateFlow<ResultsViewState>(Loading)
    val viewState: Flow<ResultsViewState> get() = _viewState

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
                val items = dataSource.search(image)
                _viewState.value = Success(items)
            } catch (e: Exception) {
                _viewState.value = Error
            }
        }
    }

    fun onItemClick(item: VisualSearchItem, activity: Activity) {
        val uri = Uri.parse(item.hostPageUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        activity.startActivity(intent)
    }
}
