package com.hamomel.vision.searchresults.presentation

import com.hamomel.vision.searchresults.data.model.VisualSearchItem

/**
 * @author Роман Зотов on 12.12.2021
 */
sealed class ResultsViewState

object Loading : ResultsViewState()
data class Success(
    val items: List<VisualSearchItem>
) : ResultsViewState()
object Error : ResultsViewState()


