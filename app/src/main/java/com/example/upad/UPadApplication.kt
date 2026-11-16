package com.example.upad

import android.app.Application
import com.example.upad.di.AppContainer

class UPadApplication : Application() {

    /** Dependencias compartidas por toda la app. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
