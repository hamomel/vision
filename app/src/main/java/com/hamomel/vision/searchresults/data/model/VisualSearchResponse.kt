package com.hamomel.vision.searchresults.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Роман Зотов on 12.12.2021
 */
@Serializable
class VisualSearchResponse(
    @SerialName("tags")
    val tags: List<Tag>
)

@Serializable
class Tag(
    @SerialName("actions")
    val actions: List<Action>
)

@Serializable
class Action(
    @SerialName("actionType")
    val actionType: String,
    @SerialName("data")
    val data: Data? = null
)

@Serializable
class Data(
    @SerialName("value")
    val value: List<VisualSearchItem> = emptyList()
)

@Serializable
data class VisualSearchItem(
    @SerialName("name")
    val name: String,
    @SerialName("contentUrl")
    val contentUrl: String,
    @SerialName("hostPageUrl")
    val hostPageUrl: String,
    @SerialName("height")
    val height: Int = 0,
    @SerialName("width")
    val width: Int = 0
)
