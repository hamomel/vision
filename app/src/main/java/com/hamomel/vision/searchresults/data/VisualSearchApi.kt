package com.hamomel.vision.searchresults.data

import com.hamomel.vision.searchresults.data.model.VisualSearchResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * @author Роман Зотов on 12.12.2021
 */
interface VisualSearchApi {

    @Multipart
    @POST("images/visualsearch")
    suspend fun search(
        @Part image: MultipartBody.Part,
        @Query("mkt") marketCode: String = "ru_RU",
        @Query("safesearch") safesearch: String = "off"
    ): VisualSearchResponse
}
