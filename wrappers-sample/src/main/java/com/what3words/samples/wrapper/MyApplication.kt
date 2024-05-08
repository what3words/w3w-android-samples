package com.what3words.samples.wrapper

import android.app.Application
import com.what3words.samples.wrapper.di.AppContainer

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this.applicationContext)
    }

    companion object {
        // Instance of AppContainer that will be used by all the Activities of the app
        @JvmStatic
        lateinit var appContainer: AppContainer
            private set
    }
}