package com.example.upad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.upad.di.AppContainer

/** Crea los ViewModels de la app con las dependencias de [AppContainer]. */
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(RoutineViewModel::class.java) -> RoutineViewModel(
            repository = container.firebaseRepository,
            dataStoreManager = container.dataStoreManager,
            languageDataStore = container.languageDataStore,
            widgetUpdater = container.widgetUpdater,
            deviceIdProvider = container.deviceIdProvider,
            arasaacRepository = container.arasaacRepository,
            sessionRepository = container.sessionRepository,
            deviceRepository = container.deviceRepository,
            parentProfileRepository = container.parentProfileRepository
        )
        modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(
            dataStoreManager = container.dataStoreManager,
            languageDataStore = container.languageDataStore,
            themePreferences = container.themePreferences,
            repository = container.firebaseRepository
        )
        modelClass.isAssignableFrom(AiViewModel::class.java) -> AiViewModel(
            aiService = container.aiRepository,
            languageFlow = container.languageDataStore.languageFlow
        )
        modelClass.isAssignableFrom(TrackingViewModel::class.java) -> TrackingViewModel(container.deviceRepository)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}
