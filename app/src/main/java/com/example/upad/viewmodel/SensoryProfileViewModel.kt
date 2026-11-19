package com.example.upad.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upad.data.ParentProfileRepository
import com.example.upad.data.SensoryProfile
import com.example.upad.data.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private const val TAG = "SensoryProfileViewModel"

/** Estado de la pantalla en la que el tutor ajusta el perfil sensorial del modo niño. */
class SensoryProfileViewModel(
    private val parentProfileRepository: ParentProfileRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _perfil = MutableStateFlow(SensoryProfile())
    val perfil: StateFlow<SensoryProfile> = _perfil.asStateFlow()

    init {
        val uid = sessionRepository.currentUserId
        if (uid.isNotEmpty()) {
            viewModelScope.launch {
                parentProfileRepository.observeSensoryProfile(uid)
                    .catch { error -> Log.e(TAG, "No se pudo leer el perfil sensorial", error) }
                    .collect { _perfil.value = it }
            }
        }
    }

    /**
     * Aplica un cambio y lo guarda. El estado se actualiza al momento para que la
     * pantalla responda sin esperar a Firestore.
     */
    fun actualizar(cambio: (SensoryProfile) -> SensoryProfile) {
        val nuevo = cambio(_perfil.value)
        if (nuevo == _perfil.value) return
        _perfil.value = nuevo
        val uid = sessionRepository.currentUserId
        if (uid.isNotEmpty()) {
            parentProfileRepository.saveSensoryProfile(uid, nuevo)
        }
    }
}
