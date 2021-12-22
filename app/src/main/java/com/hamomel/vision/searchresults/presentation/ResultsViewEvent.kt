package com.hamomel.vision.searchresults.presentation

sealed interface ResultsViewEvent

class OpenResultScreen(
    val pageUrl: String
) : ResultsViewEvent