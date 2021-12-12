package com.hamomel.vision.core.di

import android.content.Context
import com.hamomel.vision.R
import com.hamomel.vision.core.network.ApiKeyInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * @author Роман Зотов on 12.12.2021
 */
val networkModule = module {
    single {
        val apiKey = get<Context>().getString(R.string.subscription_key)

        OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(apiKey))
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    single { Json { ignoreUnknownKeys = true } }

    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://api.bing.microsoft.com/v7.0/")
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
