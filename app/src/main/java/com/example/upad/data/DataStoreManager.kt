package com.example.upad.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 🔌 Esto va fuera de la clase. Es el inicializador oficial de DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "upad_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        // 🔑 La llave exacta para guardar y leer en el disco
        val IS_PREMIUM_KEY = booleanPreferencesKey("is_user_premium")
    }

    // 🔄 FLUJO CORREGIDO: Usamos .data y .map de la librería de Preferences
    val isPremiumFlow: Flow<Boolean> = context.dataStore.data.map { preferencias ->
        preferencias[IS_PREMIUM_KEY] ?: false
    }

    // 💾 FUNCIÓN SUSPENDIDA CORREGIDA: En Kotlin se usa 'suspend fun' para corrutinas
    suspend fun setPremiumStatus(isPremium: Boolean) {
        context.dataStore.edit { preferencias ->
            preferencias[IS_PREMIUM_KEY] = isPremium
        }
    }
}