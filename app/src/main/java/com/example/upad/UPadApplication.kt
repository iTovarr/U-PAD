package com.example.upad

import android.app.Application
import android.content.Context

class UPadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
    companion object {
        lateinit var appContext: Context
            private set
    }
}