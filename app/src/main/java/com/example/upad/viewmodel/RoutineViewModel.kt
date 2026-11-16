package com.example.upad.viewmodel

import android.util.Log
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upad.data.FirebaseRepository
import com.example.upad.data.ArasaacRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.upad.data.ArasaacPictogram
import com.example.upad.data.DataStoreManager
import com.example.upad.data.DeviceRepository
import com.example.upad.data.LanguageDataStore
import com.example.upad.data.ParentProfileRepository
import com.example.upad.data.SessionRepository
import com.example.upad.widget.WidgetProgress
import com.example.upad.widget.WidgetUpdater
import com.example.upad.widget.ChildSessionMonitorWidgetProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.firestore.ListenerRegistration
import com.example.upad.utils.PairingCode
import com.example.upad.utils.RoutineProgressCalculator

private const val TAG = "RoutineViewModel"

data class TaskItem(
    val actividad: String = "",
    val palabraClave: String = "",
    val imageUrl: String = "",
    val dias: List<String> = emptyList(),
    val duration: Int = 15,
    val estadosPorDia: Map<String, Boolean> = emptyMap(),
    val emocionesPorDia: Map<String, String> = emptyMap()
) {
    fun estaCompletadaHoy(diaActual: String): Boolean {
        val diaKey = RoutineProgressCalculator.obtenerPrefijoDia(diaActual)
        return estadosPorDia[diaKey] ?: false
    }

    fun obtenerEmocionHoy(diaActual: String): String {
        val diaKey = RoutineProgressCalculator.obtenerPrefijoDia(diaActual)
        return emocionesPorDia[diaKey] ?: ""
    }
}

class RoutineViewModel(
    private val repository: FirebaseRepository,
    private val dataStoreManager: DataStoreManager,
    private val languageDataStore: LanguageDataStore,
    private val widgetUpdater: WidgetUpdater,
    private val deviceIdProvider: () -> String,
    private val arasaacRepository: ArasaacRepository,
    private val sessionRepository: SessionRepository,
    private val deviceRepository: DeviceRepository,
    private val parentProfileRepository: ParentProfileRepository
) : ViewModel() {

    private var listenerManana: ListenerRegistration? = null
    private var listenerTarde: ListenerRegistration? = null
    private var listenerNoche: ListenerRegistration? = null
    private var languageListener: ListenerRegistration? = null

    // Copias de solo lectura del plan y del idioma (los gestiona SettingsViewModel):
    // se usan para el progreso de los widgets y el idioma de los avisos al tutor.
    private val _isPremiumManual = MutableStateFlow(false)
    private val _appLanguage = MutableStateFlow("es")

    private val _mensajesRefuerzo = MutableStateFlow<List<String>>(emptyList())
    val reinforcementMessages: StateFlow<List<String>> = _mensajesRefuerzo.asStateFlow()

    fun cargarMensajesRefuerzo(padreId: String, turn: String) {
        val uidValido = obtenerUidSeguro(padreId)
        val turnoValido = normalizarTurno(turn)
        viewModelScope.launch {
            try {
                val list = repository.obtenerMensajesRefuerzo(uidValido, turnoValido)
                _mensajesRefuerzo.value = list
            } catch (e: Exception) {
                Log.e(TAG, "Error en cargarMensajesRefuerzo", e)
            }
        }
    }

    fun guardarMensajesRefuerzoPremium(userId: String, turn: String, mensajes: List<String>) {
        val uidValido = obtenerUidSeguro(userId)
        val turnoValido = normalizarTurno(turn)
        viewModelScope.launch {
            try {
                repository.guardarMensajesRefuerzo(uidValido, turnoValido, mensajes)
            } catch (e: Exception) {
                Log.e(TAG, "Error en guardarMensajesRefuerzoPremium", e)
            }
        }
    }

    init {
        // Cargar estado premium
        viewModelScope.launch {
            dataStoreManager.isPremiumFlow.collectLatest { estadoReal ->
                _isPremiumManual.value = estadoReal
                notificarCambioAlWidget()
            }
        }
        // Cargar estado de idioma
        viewModelScope.launch {
            languageDataStore.languageFlow.collectLatest { lang ->
                _appLanguage.value = lang
            }
        }
    }

    // 👦 CHILD VINCULACION & LOCATION STATE & LOGIC
    private val _codigoNiño = MutableStateFlow("------")
    val codigoNiño: StateFlow<String> = _codigoNiño.asStateFlow()

    private val _estaVinculado = MutableStateFlow(false)
    val estaVinculado: StateFlow<Boolean> = _estaVinculado.asStateFlow()

    private val _cargandoChild = MutableStateFlow(true)
    val cargandoChild: StateFlow<Boolean> = _cargandoChild.asStateFlow()

    private val _padreIdAsociado = MutableStateFlow("")
    val padreIdAsociado: StateFlow<String> = _padreIdAsociado.asStateFlow()

    private val _esPremiumPorPadre = MutableStateFlow(false)
    val esPremiumPorPadre: StateFlow<Boolean> = _esPremiumPorPadre.asStateFlow()

    private val _errorDesvinculacion = MutableStateFlow("")
    val errorDesvinculacion: StateFlow<String> = _errorDesvinculacion.asStateFlow()

    private val _cargandoDesvinculacion = MutableStateFlow(false)
    val cargandoDesvinculacion: StateFlow<Boolean> = _cargandoDesvinculacion.asStateFlow()

    private var devicesListener: ListenerRegistration? = null
    private var premiumListener: ListenerRegistration? = null
    private var pairingCodeListener: ListenerRegistration? = null
    private var codigoGeneradoEnSesion = false

    fun iniciarEscuchaDispositivoNiño(deviceId: String) {
        devicesListener?.remove()
        devicesListener = repository.listenToChildDevice(deviceId) { snapshot, error ->
            if (error != null) {
                _cargandoChild.value = false
                return@listenToChildDevice
            }

            val padreId = snapshot?.getString("padreId") ?: ""

            if (snapshot == null || !snapshot.exists() || padreId.isEmpty()) {
                _estaVinculado.value = false
                _padreIdAsociado.value = ""
                premiumListener?.remove()

                if (!codigoGeneradoEnSesion) {
                    codigoGeneradoEnSesion = true
                    viewModelScope.launch {
                        try {
                            val nuevoCodigo = repository.createUniquePairingCode(deviceId)
                            _codigoNiño.value = nuevoCodigo

                            pairingCodeListener?.remove()
                            pairingCodeListener = repository.listenToPairingCode(nuevoCodigo) { codeSnapshot, codeError ->
                                if (codeError != null) return@listenToPairingCode
                                if (codeSnapshot != null && codeSnapshot.exists()) {
                                    val estado = codeSnapshot.getString("estado")
                                    val pId = codeSnapshot.getString("padreId")
                                    if (estado == PairingCode.ESTADO_ENLAZADO && !pId.isNullOrEmpty()) {
                                        repository.linkDeviceToParent(deviceId, pId)
                                        repository.deletePairingCode(nuevoCodigo)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Sin red o sin código libre: se permite reintentar en el próximo evento.
                            Log.e(TAG, "Error en iniciarEscuchaDispositivoNiño", e)
                            codigoGeneradoEnSesion = false
                        }
                    }
                }

                _cargandoChild.value = false
                return@listenToChildDevice
            }

            // Padre vinculado
            _estaVinculado.value = true

            if (_padreIdAsociado.value != padreId) {
                _padreIdAsociado.value = padreId
                cargarRutinasDesdeFirebase(padreId)
                iniciarEscuchaIdioma(padreId)

                premiumListener?.remove()
                premiumListener = repository.listenToUserPremium(padreId) { isPremium ->
                    _esPremiumPorPadre.value = isPremium
                    _cargandoChild.value = false
                }
            }

            pairingCodeListener?.remove()
            pairingCodeListener = null
        }
    }

    fun detenerEscuchaDispositivoNiño() {
        devicesListener?.remove()
        premiumListener?.remove()
        pairingCodeListener?.remove()
        detenerEscuchaIdioma()
    }

    fun updateLocation(deviceId: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                repository.updateDeviceLocation(deviceId, latitude, longitude)
            } catch (e: Exception) {
                Log.e(TAG, "Error en updateLocation", e)
            }
        }
    }

    fun desvincularDispositivo(deviceId: String, email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _errorDesvinculacion.value = "Por favor, completa todos los campos."
            return
        }
        _cargandoDesvinculacion.value = true
        _errorDesvinculacion.value = ""

        // El tutor confirma con sus credenciales en el dispositivo del niño. La sesión
        // se cierra siempre al terminar para no dejar su cuenta abierta ahí.
        viewModelScope.launch {
            try {
                // La contraseña se envía tal cual, igual que en el inicio de sesión.
                sessionRepository.signIn(email.trim(), pass)
            } catch (e: Exception) {
                _cargandoDesvinculacion.value = false
                _errorDesvinculacion.value = e.localizedMessage ?: "Credenciales incorrectas"
                return@launch
            }

            if (sessionRepository.currentUserId != _padreIdAsociado.value) {
                sessionRepository.signOut()
                _cargandoDesvinculacion.value = false
                _errorDesvinculacion.value = "El correo no coincide con el padre enlazado a este dispositivo."
                return@launch
            }

            try {
                repository.deleteDeviceDoc(deviceId)
                sessionRepository.signOut()
                _cargandoDesvinculacion.value = false
                _errorDesvinculacion.value = ""
                onSuccess()
            } catch (e: Exception) {
                sessionRepository.signOut()
                _cargandoDesvinculacion.value = false
                _errorDesvinculacion.value = e.localizedMessage ?: "Error al desvincular"
            }
        }
    }

    fun clearUnlinkError() {
        _errorDesvinculacion.value = ""
    }

    fun iniciarEscuchaIdioma(userId: String) {
        languageListener?.remove()
        if (userId.isEmpty()) return

        languageListener = repository.listenUserLanguage(userId) { nuevoIdioma ->
            viewModelScope.launch {
                languageDataStore.saveLanguage(nuevoIdioma)
            }
        }
    }

    fun detenerEscuchaIdioma() {
        languageListener?.remove()
    }

    private val _currentRoutineName = MutableStateFlow("")
    val currentRoutineName: StateFlow<String> = _currentRoutineName

    private val _tasksManana = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasksManana: StateFlow<List<TaskItem>> = _tasksManana

    private val _tasksTarde = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasksTarde: StateFlow<List<TaskItem>> = _tasksTarde

    private val _tasksNoche = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasksNoche: StateFlow<List<TaskItem>> = _tasksNoche

    private val _searchResults = MutableStateFlow<List<ArasaacPictogram>>(emptyList())
    val searchResults: StateFlow<List<ArasaacPictogram>> = _searchResults



    fun updateName(newName: String) { _currentRoutineName.value = newName }

    private var padreIdActual: String = ""

    var ultimoDiaCargado: String = "LUNES"
        private set

    fun cargarRutinasDesdeFirebase(userId: String) {
        padreIdActual = userId
        cargarRutinasPorDia(userId, RoutineProgressCalculator.obtenerDiaDeHoy())
    }

    fun cargarRutinasPorDia(userId: String, dia: String) {
        if (userId.isNotEmpty()) {
            padreIdActual = userId
        }
        val uidValido = obtenerUidSeguro(userId)
        val diaKey = dia.uppercase().trim().replace("É", "E").replace("Á", "A")
        val diaFull = when (diaKey.take(3)) {
            "LUN" -> "LUNES"
            "MAR" -> "MARTES"
            "MIE" -> "MIERCOLES"
            "JUE" -> "JUEVES"
            "VIE" -> "VIERNES"
            "SAB" -> "SABADO"
            "DOM" -> "DOMINGO"
            else -> diaKey
        }
        ultimoDiaCargado = diaFull

        viewModelScope.launch {
            listenerManana?.remove()
            listenerTarde?.remove()
            listenerNoche?.remove()

            listenerManana = repository.escucharRutinasDelPadrePorDia(uidValido, "MAÑANA", diaFull) { lista ->
                _tasksManana.value = lista
                notificarCambioAlWidget()
            }
            listenerTarde = repository.escucharRutinasDelPadrePorDia(uidValido, "TARDE", diaFull) { lista ->
                _tasksTarde.value = lista
                notificarCambioAlWidget()
            }
            listenerNoche = repository.escucharRutinasDelPadrePorDia(uidValido, "NOCHE", diaFull) { lista ->
                _tasksNoche.value = lista
                notificarCambioAlWidget()
            }
        }
    }

    private fun notificarCambioAlWidget() {
        try {
            val prefijoDia = RoutineProgressCalculator.obtenerPrefijoDia(RoutineProgressCalculator.obtenerDiaDeHoy())
            widgetUpdater.publicar(
                WidgetProgress.calcular(
                    tareasManana = _tasksManana.value,
                    tareasTarde = _tasksTarde.value,
                    tareasNoche = _tasksNoche.value,
                    prefijoDia = prefijoDia,
                    isPremium = _isPremiumManual.value
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en notificarCambioAlWidget", e)
        }
    }

    fun searchArasaac(query: String) {
        viewModelScope.launch {
            try {
                if (query.length > 2) {
                    _searchResults.value = arasaacRepository.searchPictograms(query)
                } else {
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }

    private fun normalizarTurno(turn: String): String {
        val clean = turn.uppercase().trim()
        return when (clean) {
            "MAÑANA", "MANANA" -> "MAÑANA"
            "TARDE" -> "TARDE"
            "NOCHE" -> "NOCHE"
            else -> "MAÑANA"
        }
    }

    private fun obtenerUidSeguro(userId: String): String {
        return if (userId.isBlank()) {
            sessionRepository.currentUserId
        } else {
            userId
        }
    }

    fun addTask(turn: String, actividadTexto: String, imageUrl: String, userId: String = "") {
        val turnoValido = normalizarTurno(turn)
        val uidValido = obtenerUidSeguro(userId)
        val newTask = TaskItem(
            actividad = actividadTexto.uppercase(),
            imageUrl = imageUrl,
            dias = listOf("LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM"),
        )
        val todosLosDias = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO")

        viewModelScope.launch {
            for (diaFull in todosLosDias) {
                val listaActualizada = try {
                    val listExistente = repository.obtenerRutinasDelPadreDirectoPorDia(uidValido, turnoValido, diaFull)
                    listExistente + newTask
                } catch (e: Exception) {
                    emptyList<TaskItem>() + newTask
                }
                try {
                    repository.saveRoutinePorDia(uidValido, turnoValido, diaFull, listaActualizada)
                } catch (e: Exception) {
                    Log.e(TAG, "Error en addTask", e)
                }
            }
        }
    }

    fun addTaskConDia(turn: String, actividadTexto: String, imageUrl: String, dia: String, userId: String = "") {
        val turnoValido = normalizarTurno(turn)
        val uidValido = obtenerUidSeguro(userId)
        val diaKey = RoutineProgressCalculator.obtenerPrefijoDia(dia)
        val diaFull = when (diaKey) {
            "LUN" -> "LUNES"
            "MAR" -> "MARTES"
            "MIE", "MIÉ" -> "MIERCOLES"
            "JUE" -> "JUEVES"
            "VIE" -> "VIERNES"
            "SAB", "SÁB" -> "SABADO"
            "DOM" -> "DOMINGO"
            else -> dia.uppercase().trim().replace("É", "E").replace("Á", "A")
        }

        val newTask = TaskItem(
            actividad = actividadTexto.uppercase(),
            imageUrl = imageUrl,
            dias = listOf(diaKey),
        )

        viewModelScope.launch {
            val listaActualizada = try {
                repository.obtenerRutinasDelPadreDirectoPorDia(uidValido, turnoValido, diaFull) + newTask
            } catch (e: Exception) {
                emptyList<TaskItem>() + newTask
            }

            try {
                repository.saveRoutinePorDia(uidValido, turnoValido, diaFull, listaActualizada)
            } catch (e: Exception) {
                Log.e(TAG, "Error en addTaskConDia", e)
            }
        }
    }

    fun removeTask(turn: String, index: Int) {
        val turnoValido = normalizarTurno(turn)
        val uidValido = obtenerUidSeguro("")
        val diaFull = ultimoDiaCargado

        viewModelScope.launch {
            val listLocal = when (turnoValido) {
                "MAÑANA" -> _tasksManana.value.toMutableList()
                "TARDE" -> _tasksTarde.value.toMutableList()
                else -> _tasksNoche.value.toMutableList()
            }

            if (index in listLocal.indices) {
                listLocal.removeAt(index)

                when (turnoValido) {
                    "MAÑANA" -> _tasksManana.value = listLocal
                    "TARDE"  -> _tasksTarde.value = listLocal
                    "NOCHE"  -> _tasksNoche.value = listLocal
                }

                try {
                    repository.saveRoutinePorDia(uidValido, turnoValido, diaFull, listLocal)
                } catch (e: Exception) {
                    Log.e(TAG, "Error en removeTask", e)
                }
            }
        }
    }

    fun completeTaskPorNombre(userId: String, turn: String, actividadTexto: String, diaActual: String) {
        val turnoValido = normalizarTurno(turn)
        val uidValido = obtenerUidSeguro(userId)
        val diaKey = RoutineProgressCalculator.obtenerPrefijoDia(diaActual)
        val diaFull = when (diaKey) {
            "LUN" -> "LUNES"
            "MAR" -> "MARTES"
            "MIE" -> "MIERCOLES"
            "JUE" -> "JUEVES"
            "VIE" -> "VIERNES"
            "SAB" -> "SABADO"
            "DOM" -> "DOMINGO"
            else -> diaActual.uppercase().trim().replace("É", "E").replace("Á", "A")
        }

        viewModelScope.launch {
            // Resolver el padreId real (uidValido) de forma robusta
            var finalUid = uidValido
            if (finalUid.isEmpty()) {
                finalUid = padreIdActual
            }
            if (finalUid.isEmpty()) {
                try {
                    val deviceId = deviceIdProvider()
                    finalUid = deviceRepository.getParentId(deviceId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error en completeTaskPorNombre", e)
                }
            }

            val listaActual = try {
                repository.obtenerRutinasDelPadreDirectoPorDia(finalUid, turnoValido, diaFull).toMutableList()
            } catch (e: Exception) {
                when (turnoValido) {
                    "MAÑANA" -> _tasksManana.value.toMutableList()
                    "TARDE"  -> _tasksTarde.value.toMutableList()
                    else     -> _tasksNoche.value.toMutableList()
                }
            }

            val indexReal = listaActual.indexOfFirst {
                it.actividad.uppercase() == actividadTexto.uppercase()
            }

            if (indexReal != -1) {
                val tareaEncontrada = listaActual[indexReal]
                val nuevosEstados = tareaEncontrada.estadosPorDia.toMutableMap()
                nuevosEstados[diaKey] = true

                listaActual[indexReal] = tareaEncontrada.copy(estadosPorDia = nuevosEstados)

                when (turnoValido) {
                    "MAÑANA" -> _tasksManana.value = listaActual
                    "TARDE"  -> _tasksTarde.value = listaActual
                    "NOCHE"  -> _tasksNoche.value = listaActual
                }

                try {
                    repository.saveRoutinePorDia(finalUid, turnoValido, diaFull, listaActual)

                    if (finalUid.isNotEmpty()) {
                        val lang = _appLanguage.value

                        // 1. Enviar notificación por la tarea completada
                        val (taskTitle, taskDesc) = when (lang) {
                            "en" -> "Activity completed! 🌟" to "Your child has completed the activity: ${actividadTexto.lowercase()}."
                            "fr" -> "Activité complétée! 🌟" to "Votre enfant a complété l'activité: ${actividadTexto.lowercase()}."
                            "de" -> "Aktivität abgeschlossen! 🌟" to "Ihr Kind hat die Aktivität abgeschlossen: ${actividadTexto.lowercase()}."
                            "pt" -> "Atividade concluída! 🌟" to "Seu filho concluiu a atividade: ${actividadTexto.lowercase()}."
                            "ru" -> "Активность завершена! 🌟" to "Ваш ребенок выполнил активность: ${actividadTexto.lowercase()}."
                            else -> "¡Actividad completada! 🌟" to "Tu hijo ha completado la actividad: ${actividadTexto.lowercase()}."
                        }

                        parentProfileRepository.addNotification(finalUid, taskTitle, taskDesc, type = "INFO")

                        // 2. Verificar si todas las tareas de este turno aplicables a hoy están completadas
                        val targetPrefijo = RoutineProgressCalculator.obtenerPrefijoDia(diaActual)
                        val tareasDeHoy = listaActual.filter { tarea ->
                            tarea.dias.isEmpty() || tarea.dias.any { d ->
                                val dClean = d.uppercase().trim().replace("É", "E").replace("Á", "A")
                                dClean == targetPrefijo || dClean.take(3) == targetPrefijo.take(3)
                            }
                        }

                        val todasCompletadas = tareasDeHoy.isNotEmpty() && tareasDeHoy.all { it.estaCompletadaHoy(diaActual) }

                        if (todasCompletadas) {
                            val (routineTitle, routineDesc) = when (lang) {
                                "en" -> "Routine completed! 🎉" to "Your child has successfully completed all tasks for the ${turnoValido.lowercase()} routine."
                                "fr" -> "Routine terminée! 🎉" to "Votre enfant a terminé avec succès toutes les tâches de la routine du ${if (turnoValido == "MAÑANA") "matin" else if (turnoValido == "TARDE") "midi" else "soir"}."
                                "de" -> "Routine abgeschlossen! 🎉" to "Ihr Kind hat alle Aufgaben für die ${if (turnoValido == "MAÑANA") "Morgen" else if (turnoValido == "TARDE") "Nachmittag" else "Abend"}-Routine erfolgreich abgeschlossen."
                                "pt" -> "Rotina concluída! 🎉" to "Seu filho concluiu com sucesso todas as tarefas da rotina da ${turnoValido.lowercase()}."
                                "ru" -> "Рутина завершена! 🎉" to "Ваш ребенок успешно выполнил все задачи для рутины (${if (turnoValido == "MAÑANA") "утро" else if (turnoValido == "TARDE") "день" else "вечер"})."
                                else -> "¡Rutina de la ${turnoValido.lowercase()} completada! 🎉" to "Tu hijo ha terminado con éxito todas las tareas de la rutina de la ${turnoValido.lowercase()}."
                            }

                            parentProfileRepository.addNotification(finalUid, routineTitle, routineDesc, type = "SUCCESS")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en completeTaskPorNombre", e)
                }
            }
        }
    }

    fun getCompletedCount(turn: String, diaActual: String): Int {
        val turnoValido = normalizarTurno(turn)
        val targetList = when (turnoValido) {
            "MAÑANA" -> _tasksManana.value
            "TARDE" -> _tasksTarde.value
            else -> _tasksNoche.value
        }
        val prefijoLimpio = RoutineProgressCalculator.obtenerPrefijoDia(diaActual)
        return targetList.count { it.estaCompletadaHoy(prefijoLimpio) }
    }

    fun getTotalCount(turn: String, diaActual: String): Int {
        val turnoValido = normalizarTurno(turn)
        val targetList = when (turnoValido) {
            "MAÑANA" -> _tasksManana.value
            "TARDE" -> _tasksTarde.value
            else -> _tasksNoche.value
        }
        val prefijoLimpio = RoutineProgressCalculator.obtenerPrefijoDia(diaActual)
        return targetList.count { tarea ->
            tarea.dias.isEmpty() || tarea.dias.any {
                RoutineProgressCalculator.obtenerPrefijoDia(it) == prefijoLimpio
            }
        }
    }

    fun saveAll(userId: String, turn: String) {
        val turnoValido = normalizarTurno(turn)
        val uidValido = obtenerUidSeguro(userId)
        val diaFull = ultimoDiaCargado
        viewModelScope.launch {
            val listToSave = when (turnoValido) {
                "MAÑANA" -> _tasksManana.value
                "TARDE" -> _tasksTarde.value
                else -> _tasksNoche.value
            }
            repository.saveRoutinePorDia(uidValido, turnoValido, diaFull, listToSave)
        }
    }

    fun agregarActividadAutomatica(userId: String, turn: String, textoCompleto: String, diasSeleccionados: List<String>) {
        val turnoValido = normalizarTurno(turn)
        val uidValido = obtenerUidSeguro(userId)

        viewModelScope.launch {
            var urlImagenFinal = ""
            try {
                val textoNormalizado = textoCompleto.trim()
                    .lowercase()
                    .replace(Regex("[áàä]"), "a")
                    .replace(Regex("[éèë]"), "e")
                    .replace(Regex("[íìï]"), "i")
                    .replace(Regex("[óòö]"), "o")
                    .replace(Regex("[úùü]"), "u")
                    .replace(Regex("[^a-z0-9 ]"), "")

                val palabras = textoNormalizado.split(" ")
                    .map { it.trim() }
                    .filter { it.length > 2 }

                for (palabra in palabras) {
                    val resultados = arasaacRepository.searchPictograms(palabra)
                    if (resultados.isNotEmpty()) {
                        val idImagen = resultados.first()._id
                        urlImagenFinal = "https://static.arasaac.org/pictograms/$idImagen/${idImagen}_300.png"
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en agregarActividadAutomatica", e)
            }

            val diasFinales = if (diasSeleccionados.isEmpty()) {
                listOf(ultimoDiaCargado)
            } else {
                diasSeleccionados
            }

            val mapaInicialEstados = diasFinales.associate {
                RoutineProgressCalculator.obtenerPrefijoDia(it) to false
            }

            val nuevaTarea = TaskItem(
                actividad = textoCompleto.uppercase(),
                palabraClave = textoCompleto.trim(),
                imageUrl = urlImagenFinal,
                dias = diasFinales.map { RoutineProgressCalculator.obtenerPrefijoDia(it) },
                estadosPorDia = mapaInicialEstados,
            )

            for (dia in diasFinales) {
                val diaNormalizado = RoutineProgressCalculator.obtenerPrefijoDia(dia)
                val diaFull = when (diaNormalizado) {
                    "LUN" -> "LUNES"
                    "MAR" -> "MARTES"
                    "MIE" -> "MIERCOLES"
                    "JUE" -> "JUEVES"
                    "VIE" -> "VIERNES"
                    "SAB" -> "SABADO"
                    "DOM" -> "DOMINGO"
                    else -> dia.uppercase().trim().replace("É", "E").replace("Á", "A")
                }

                val listaActualizada = try {
                    val listExistente = repository.obtenerRutinasDelPadreDirectoPorDia(uidValido, turnoValido, diaFull)
                    listExistente + nuevaTarea
                } catch (e: Exception) {
                    emptyList<TaskItem>() + nuevaTarea
                }

                try {
                    repository.saveRoutinePorDia(uidValido, turnoValido, diaFull, listaActualizada)
                } catch (e: Exception) {
                    Log.e(TAG, "Error en agregarActividadAutomatica", e)
                }
            }
        }
    }

    fun registrarFeedbackEmocional(
        userId: String, turn: String,
        actividadNombre: String, emocionSeleccionada: String, context: Context
    ) {
        val turnoValido = normalizarTurno(turn)
        val uidValido = obtenerUidSeguro(userId)
        val diaActual = RoutineProgressCalculator.obtenerDiaDeHoy()
        val diaKey = RoutineProgressCalculator.obtenerPrefijoDia(diaActual)
        val diaFull = when (diaKey) {
            "LUN" -> "LUNES"
            "MAR" -> "MARTES"
            "MIE" -> "MIERCOLES"
            "JUE" -> "JUEVES"
            "VIE" -> "VIERNES"
            "SAB" -> "SABADO"
            "DOM" -> "DOMINGO"
            else -> diaActual.uppercase().trim().replace("É", "E").replace("Á", "A")
        }

        viewModelScope.launch {
            val listaActual = try {
                repository.obtenerRutinasDelPadreDirectoPorDia(uidValido, turnoValido, diaFull).toMutableList()
            } catch (e: Exception) {
                when (turnoValido) {
                    "MAÑANA" -> _tasksManana.value.toMutableList()
                    "TARDE"  -> _tasksTarde.value.toMutableList()
                    else     -> _tasksNoche.value.toMutableList()
                }
            }

            val index = listaActual.indexOfFirst {
                it.actividad.uppercase() == actividadNombre.uppercase()
            }

            if (index != -1) {
                val tarea = listaActual[index]
                val nuevasEmociones = tarea.emocionesPorDia.toMutableMap()
                nuevasEmociones[diaKey] = emocionSeleccionada

                listaActual[index] = tarea.copy(emocionesPorDia = nuevasEmociones)

                when (turnoValido) {
                    "MAÑANA" -> _tasksManana.value = listaActual
                    "TARDE"  -> _tasksTarde.value = listaActual
                    "NOCHE"  -> _tasksNoche.value = listaActual
                }

                try {
                    repository.saveRoutinePorDia(uidValido, turnoValido, diaFull, listaActual)
                    val intent = Intent(context, ChildSessionMonitorWidgetProvider::class.java).apply {
                        action = ChildSessionMonitorWidgetProvider.ACTION_REFRESH
                    }
                    context.sendBroadcast(intent)
                } catch (e: Exception) { Log.e(TAG, "Error en registrarFeedbackEmocional", e) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerManana?.remove()
        listenerTarde?.remove()
        listenerNoche?.remove()
        detenerEscuchaIdioma()
    }
}