package com.example.upad.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upad.dashboard.DispositivoVinculado
import com.example.upad.data.DeviceLocation
import com.example.upad.data.DeviceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private const val TAG = "TrackingViewModel"

/** Estado del mapa de seguimiento: dispositivos del tutor y ubicación del seleccionado. */
class TrackingViewModel(private val deviceRepository: DeviceRepository) : ViewModel() {

    private var ubicacionJob: Job? = null
    private var dispositivosJob: Job? = null

    private val _ubicacion = MutableStateFlow<DeviceLocation?>(null)
    val ubicacion: StateFlow<DeviceLocation?> = _ubicacion

    private val _dispositivosConectados = MutableStateFlow<List<DispositivoVinculado>>(emptyList())
    val dispositivosConectados: StateFlow<List<DispositivoVinculado>> = _dispositivosConectados

    /** Escucha en tiempo real la ubicación del dispositivo indicado (sustituye a la escucha anterior). */
    fun iniciarRastreoHijo(hijoId: String) {
        ubicacionJob?.cancel()
        ubicacionJob = viewModelScope.launch {
            deviceRepository.observeLocation(hijoId)
                .catch { error -> Log.e(TAG, "Error en iniciarRastreoHijo", error) }
                .collect { _ubicacion.value = it }
        }
    }

    /** Escucha en tiempo real la lista de dispositivos vinculados al tutor. */
    fun escucharDispositivosVinculados(padreId: String) {
        dispositivosJob?.cancel()
        if (padreId.isEmpty()) {
            _dispositivosConectados.value = emptyList()
            return
        }

        dispositivosJob = viewModelScope.launch {
            deviceRepository.observeDevicesOf(padreId)
                .catch { error -> Log.e(TAG, "Error en escucharDispositivosVinculados", error) }
                .collect { dispositivos ->
                    _dispositivosConectados.value = dispositivos.map {
                        DispositivoVinculado(id = it.id, modelo = it.modelo ?: "Dispositivo")
                    }
                }
        }
    }
    // Las escuchas se cancelan solas con viewModelScope al destruirse el ViewModel.
}
