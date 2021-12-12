package com.hamomel.vision.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author Роман Зотов on 12.12.2021
 */
private const val API_KEY_HEADER_NAME = "Ocp-Apim-Subscription-Key"

class ApiKeyInterceptor(
    private val key: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader(API_KEY_HEADER_NAME, key)
            .build()

        return chain.proceed(request)
    }
}
