package com.example.upad.di

import android.content.Context
import android.provider.Settings
import com.example.upad.data.AiRepository
import com.example.upad.data.AiService
import com.example.upad.data.ArasaacRepository
import com.example.upad.data.DataStoreManager
import com.example.upad.data.DeviceRepository
import com.example.upad.data.FirebaseRepository
import com.example.upad.data.LanguageDataStore
import com.example.upad.data.ParentProfileRepository
import com.example.upad.data.SessionRepository
import com.example.upad.data.ThemePreferences
import com.example.upad.widget.WidgetUpdater

/**
 * Contenedor de dependencias de la app (inyección manual).
 *
 * Crea una única instancia de cada repositorio y servicio y se la entrega a los
 * ViewModels, que así no necesitan construir sus dependencias ni acceder a un
 * Context global. Vive en [com.example.upad.UPadApplication].
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val sessionRepository: SessionRepository by lazy { SessionRepository() }
    val firebaseRepository: FirebaseRepository by lazy { FirebaseRepository() }
    val deviceRepository: DeviceRepository by lazy { DeviceRepository() }
    val parentProfileRepository: ParentProfileRepository by lazy { ParentProfileRepository() }
    val arasaacRepository: ArasaacRepository by lazy { ArasaacRepository() }
    val aiRepository: AiService by lazy { AiRepository() }

    val dataStoreManager: DataStoreManager by lazy { DataStoreManager(appContext) }
    val languageDataStore: LanguageDataStore by lazy { LanguageDataStore(appContext) }
    val themePreferences: ThemePreferences by lazy { ThemePreferences(appContext) }

    val widgetUpdater: WidgetUpdater by lazy { WidgetUpdater(appContext) }

    /** Identificador del dispositivo: es el ID del documento en `dispositivos_niños`. */
    val deviceIdProvider: () -> String = {
        Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
