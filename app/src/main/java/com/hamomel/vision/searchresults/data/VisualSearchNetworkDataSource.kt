package com.hamomel.vision.searchresults.data

import android.graphics.Bitmap
import com.hamomel.vision.searchresults.data.model.VisualSearchItem
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

/**
 * @author Роман Зотов on 12.12.2021
 */
class VisualSearchNetworkDataSource(
    private val api: VisualSearchApi
) {

    suspend fun search(bitmap: Bitmap): List<VisualSearchItem> {
        val jpegImage = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.toByteArray()
        }

        val body = jpegImage.toRequestBody("image/jpeg".toMediaType())
        val imagePart = MultipartBody.Part.createFormData("image", "img.jpg", body)

        val response = api.search(imagePart)

        val action = response.tags
            .flatMap { it.actions }
            .firstOrNull { it.actionType == "VisualSearch" }?.data

        return action?.value ?: emptyList()
    }
}
