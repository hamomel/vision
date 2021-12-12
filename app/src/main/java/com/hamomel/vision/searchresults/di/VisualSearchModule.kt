package com.hamomel.vision.searchresults.di

import com.hamomel.vision.searchresults.data.VisualSearchApi
import com.hamomel.vision.searchresults.data.VisualSearchNetworkDataSource
import com.hamomel.vision.searchresults.presentation.VisualSearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * @author Роман Зотов on 12.12.2021
 */
val visualSearchModule = module {
    single { get<Retrofit>().create(VisualSearchApi::class.java) }
    single { VisualSearchNetworkDataSource(get()) }

    viewModel { params ->
        VisualSearchViewModel(
            image = params.getOrNull(),
            dataSource = get()
        )
    }
}
