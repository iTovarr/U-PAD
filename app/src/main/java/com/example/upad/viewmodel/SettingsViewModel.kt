package com.example.upad.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upad.data.DataStoreManager
import com.example.upad.data.FirebaseRepository
import com.example.upad.data.LanguageDataStore
import com.example.upad.data.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "SettingsViewModel"

/** Preferencias de la app: tema oscuro, idioma y plan de suscripción. */
class SettingsViewModel(
    private val dataStoreManager: DataStoreManager,
    private val languageDataStore: LanguageDataStore,
    private val themePreferences: ThemePreferences,
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _isUserPremium = MutableStateFlow(false)
    val isUserPremium: StateFlow<Boolean> = _isUserPremium.asStateFlow()

    private val _appLanguage = MutableStateFlow("es")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(themePreferences.isDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.isPremiumFlow.collectLatest { _isUserPremium.value = it }
        }
        viewModelScope.launch {
            languageDataStore.languageFlow.collectLatest { _appLanguage.value = it }
        }
    }

    /** Cambia el tema y lo guarda en el dispositivo. */
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        themePreferences.setDarkMode(enabled)
    }

    /** Refleja en el dispositivo el plan que ya consta en el servidor. */
    fun setPremiumUser(value: Boolean) {
        _isUserPremium.value = value
        viewModelScope.launch {
            dataStoreManager.setPremiumStatus(value)
        }
    }

    /** Activa o cancela el plan premium en el dispositivo y, si hay sesión, en el servidor. */
    fun setSuscripcionManual(activarPremium: Boolean, userId: String? = null) {
        viewModelScope.launch {
            dataStoreManager.setPremiumStatus(activarPremium)
            _isUserPremium.value = activarPremium

            if (!userId.isNullOrEmpty()) {
                try {
                    repository.setPremiumStatus(userId, activarPremium)
                } catch (e: Exception) {
                    Log.e(TAG, "Error en setSuscripcionManual", e)
                }
            }
        }
    }

    fun purchasePremium(userId: String? = null) { setSuscripcionManual(true, userId) }
    fun cancelPremium(userId: String? = null) { setSuscripcionManual(false, userId) }

    /** Guarda el idioma en el dispositivo y, si hay sesión, en el perfil del tutor. */
    fun changeLanguage(userId: String?, languageCode: String) {
        viewModelScope.launch {
            languageDataStore.saveLanguage(languageCode)
            if (!userId.isNullOrEmpty()) {
                try {
                    repository.saveUserLanguage(userId, languageCode)
                } catch (e: Exception) {
                    Log.e(TAG, "Error en changeLanguage", e)
                }
            }
        }
    }
}
