package com.example.upad.navigation

import android.net.Uri

/**
 * Rutas de navegación de la app. Único lugar donde se escriben como texto:
 * el resto del código usa estas constantes y funciones.
 */
object Routes {
    // Arranque y autenticación
    const val SINCRONIZANDO = "sincronizando"
    const val ROLE_SELECTION = "role_selection"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    // Configuración inicial y planes
    const val SUBSCRIPTION_PLANS = "subscription_plans"
    const val TRIAL_DISCLAIMER = "trial_disclaimer"
    const val CHILD_PROFILE_SETUP = "child_profile_setup"
    const val EXPERIENCE_SETUP = "experience_setup"
    const val DEVICE_PAIRING = "device_pairing"
    const val TUTORIALS = "tutorials"
    const val CHANGE_PLAN = "change_plan"
    const val PAYMENT_VIEW = "payment_view"

    // Panel del tutor
    const val PARENT_DASHBOARD = "parent_dashboard"
    const val HELP_TUTORIAL = "help_tutorial"
    const val TODAY_CALENDAR = "today_calendar"
    const val EMOTIONS_TRACK = "emotions_track"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val SENSORY_PROFILE = "sensory_profile"
    const val CONNECTION_CODE = "connection_code"
    const val ANALYTICS = "analytics"
    const val DEVICE_MANAGEMENT = "device_management"
    const val ACHIEVEMENT_REPORT = "achievement_report"
    const val ACTIVITY_DETAILS = "activity_details"

    // Modo niño: todas sus rutas comparten este prefijo (se usa para el modo kiosco)
    const val CHILD_PREFIX = "child_"
    const val CHILD_START = "child_start"
    const val CHILD_ROUTINE_COMPLETED = "child_routine_completed"

    // Nombres de los argumentos
    const val ARG_HIJO_ID = "hijoId"
    const val ARG_ROUTINE_TURN = "routineTurn"
    const val ARG_SELECTED_DAY = "selectedDay"
    const val ARG_ACTIVITY_NAME = "activityName"

    // Rutas con argumentos: patrón para el NavHost y función para navegar
    private const val TRACKING_BASE = "tracking"
    private const val CREATE_ROUTINE_BASE = "create_routine"
    private const val PICTOGRAM_SELECTION_BASE = "pictogram_selection"
    const val CHILD_TASK_EXECUTION_BASE = "child_task_execution"
    const val CHILD_TASK_FEEDBACK_BASE = "child_task_feedback"

    const val TRACKING = "$TRACKING_BASE/{$ARG_HIJO_ID}"
    const val CREATE_ROUTINE = "$CREATE_ROUTINE_BASE/{$ARG_ROUTINE_TURN}/{$ARG_SELECTED_DAY}"
    const val PICTOGRAM_SELECTION = "$PICTOGRAM_SELECTION_BASE/{$ARG_ROUTINE_TURN}/{$ARG_SELECTED_DAY}"
    const val CHILD_TASK_EXECUTION = "$CHILD_TASK_EXECUTION_BASE/{$ARG_ACTIVITY_NAME}/{$ARG_ROUTINE_TURN}"
    const val CHILD_TASK_FEEDBACK = "$CHILD_TASK_FEEDBACK_BASE/{$ARG_ACTIVITY_NAME}"

    fun tracking(hijoId: String) = "$TRACKING_BASE/${arg(hijoId)}"

    fun createRoutine(turno: String, dia: String) = "$CREATE_ROUTINE_BASE/${arg(turno)}/${arg(dia)}"

    fun pictogramSelection(turno: String, dia: String) = "$PICTOGRAM_SELECTION_BASE/${arg(turno)}/${arg(dia)}"

    fun childTaskExecution(actividad: String, turno: String) =
        "$CHILD_TASK_EXECUTION_BASE/${arg(actividad)}/${arg(turno)}"

    fun childTaskFeedback(actividad: String) = "$CHILD_TASK_FEEDBACK_BASE/${arg(actividad)}"

    /**
     * Codifica un argumento para que pueda viajar dentro de la ruta. Sin esto, un
     * nombre de actividad con "/" (por ejemplo "Leer/escribir"), "?" o "#" rompe la
     * ruta y la navegación falla. Navigation lo decodifica al entregar el argumento.
     */
    private fun arg(valor: String): String = Uri.encode(valor)
}
