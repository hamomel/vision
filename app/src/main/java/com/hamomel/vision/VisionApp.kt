package com.hamomel.vision

import android.app.Application
import com.hamomel.vision.data.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * @author Роман Зотов on 12.12.2021
 */
class VisionApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@VisionApp)
            modules(networkModule)
        }
    }
}

