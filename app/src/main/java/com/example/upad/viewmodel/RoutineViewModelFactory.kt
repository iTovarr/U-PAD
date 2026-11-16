package com.example.upad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.upad.di.AppContainer

class RoutineViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoutineViewModel(
                repository = container.firebaseRepository,
                dataStoreManager = container.dataStoreManager,
                languageDataStore = container.languageDataStore,
                themePreferences = container.themePreferences,
                widgetUpdater = container.widgetUpdater,
                deviceIdProvider = container.deviceIdProvider,
                arasaacRepository = container.arasaacRepository,
                aiRepository = container.aiRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
