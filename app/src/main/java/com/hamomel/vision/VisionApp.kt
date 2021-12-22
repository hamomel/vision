package com.hamomel.vision

import android.app.Application
import com.hamomel.vision.camerascreen.di.cameraScreenModule
import com.hamomel.vision.core.di.networkModule
import com.hamomel.vision.searchresults.di.visualSearchModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * @author Роман Зотов on 12.12.2021
 */
class VisionApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        startKoin {
            androidContext(this@VisionApp)
            modules(networkModule, visualSearchModule, cameraScreenModule)
        }
    }
}

