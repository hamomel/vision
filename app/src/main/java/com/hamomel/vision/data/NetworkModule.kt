package com.hamomel.vision.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.BuildConfig
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * @author Роман Зотов on 12.12.2021
 */
val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level =
                        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                        else HttpLoggingInterceptor.Level.NONE
                }
            )
    }

    single { Json { ignoreUnknownKeys = true } }

    single {
        Retrofit.Builder()
            .baseUrl("https://api.bing.microsoft.com/v7.0/")
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
    }
}
