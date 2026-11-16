package com.example.upad

import android.util.Log
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.upad.ui.theme.UPadTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.example.upad.dashboard.HijoTrackingScreen
import com.example.upad.auth.ForgotPasswordScreen
import com.example.upad.auth.LoginScreen
import com.example.upad.auth.RegisterScreen
import com.example.upad.auth.RoleSelectionScreen
import com.example.upad.auth.WelcomeScreen
import com.example.upad.child.ChildStartScreen
import com.example.upad.child.RoutineCompletedScreen
import com.example.upad.child.TaskExecutionScreen
import com.example.upad.child.TaskFeedbackScreen
import com.example.upad.dashboard.AchievementReportScreen
import com.example.upad.dashboard.ActivityDetailsScreen
import com.example.upad.dashboard.NotificationsScreen
import com.example.upad.dashboard.RoutineDashboardScreen
import com.example.upad.dashboard.DeviceManagementScreen
import com.example.upad.routines.CreateRoutineScreen
import com.example.upad.routines.PictogramSelectionScreen
import com.example.upad.setup.ChildProfileSetupScreen
import com.example.upad.setup.DevicePairingScreen
import com.example.upad.setup.ExperienceSetupScreen
import com.example.upad.setup.SubscriptionPlansScreen
import com.example.upad.setup.TrialDisclaimerScreen
import com.example.upad.setup.TutorialsScreen
import com.example.upad.setup.ChangePlanScreen
import com.example.upad.dashboard.ProfileScreen
import com.example.upad.dashboard.SettingsScreen
import com.example.upad.viewmodel.AiViewModel
import com.example.upad.viewmodel.AppViewModelFactory
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.SettingsViewModel
import com.example.upad.dashboard.ConnectionScreen
import com.example.upad.dashboard.AnalyticsScreen
import com.example.upad.premium.PaymentViewScreen
import android.content.Context
import android.content.Intent
import com.example.upad.components.UPADBottomBar
import com.example.upad.data.SessionRepository
import com.example.upad.di.LocalAppContainer
import com.example.upad.navigation.Routes
import com.example.upad.notifications.NotificationHelper
import com.example.upad.dashboard.EmotionsTrackScreen
import com.example.upad.dashboard.HelpTutorialScreen
import com.example.upad.dashboard.TodayCalendarScreen

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private var currentRoute = ""
    private var pendingLanguage = ""

    private var ordenBloqueoPadreActiva by mutableStateOf(false)
    private val container by lazy { (application as UPadApplication).container }
    private var notificationsJob: Job? = null
    private var parentPremiumJob: Job? = null
    private val listenerStartTime = System.currentTimeMillis()
    private val notificationHelper by lazy { NotificationHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        notificationHelper.crearCanal()
        requestNotificationPermission()

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Orden de bloqueo remoto del tutor. lifecycleScope cancela la escucha al destruirse la Activity.
        lifecycleScope.launch {
            container.deviceRepository.observeKioskActive(deviceId)
                .catch { error -> Log.e(TAG, "Error al escuchar el bloqueo remoto", error) }
                .collect { ordenBloqueoPadreActiva = it }
        }

        setContent {
            val viewModelFactory = remember { AppViewModelFactory(container) }
            val routineViewModel: RoutineViewModel = viewModel(factory = viewModelFactory)
            // Con ámbito de Activity, igual que routineViewModel: el estado de la IA
            // se conserva al navegar entre pantallas.
            val aiViewModel: AiViewModel = viewModel(factory = viewModelFactory)
            val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)

            // Reacciona a cada inicio o cierre de sesión. La recolección se cancela sola
            // al salir de la composición, así que no queda ningún listener colgado.
            LaunchedEffect(settingsViewModel) {
                container.sessionRepository.observeSession().collect { user ->
                    if (user != null) {
                        val sharedPrefs = getSharedPreferences("UPadPrefs", Context.MODE_PRIVATE)
                        val rol = sharedPrefs.getString("rol_usuario", null)
                        if (rol == "padre" || rol == "padre_directo") {
                            iniciarEscuchaNotificacionesPadre(user.uid)
                            iniciarEscuchaPremiumPadre(user.uid, settingsViewModel)
                        }
                    } else {
                        notificationsJob?.cancel()
                        parentPremiumJob?.cancel()
                    }
                }
            }

            val appLanguage by settingsViewModel.appLanguage.collectAsState()
            LaunchedEffect(appLanguage) {
                evaluarYAplicarIdioma(appLanguage)
            }

            val sharedPrefs = remember { getSharedPreferences("UPadPrefs", Context.MODE_PRIVATE) }
            val sistemaEstaEnOscuro = isSystemInDarkTheme()

            val isDarkModeFlow by settingsViewModel.isDarkMode.collectAsState()
            var estadoTemaOscuroReal by remember {
                mutableStateOf(sharedPrefs.getBoolean("pref_tema_oscuro", sistemaEstaEnOscuro))
            }

            LaunchedEffect(isDarkModeFlow) {
                estadoTemaOscuroReal = isDarkModeFlow
                sharedPrefs.edit().putBoolean("pref_tema_oscuro", isDarkModeFlow).apply()
            }

            val isPremiumUser by settingsViewModel.isUserPremium.collectAsState(initial = null)

            CompositionLocalProvider(LocalAppContainer provides container) {
                UPadTheme(darkTheme = estadoTemaOscuroReal, isPremium = isPremiumUser ?: false) {
                    UPadNavigation(
                        bloqueoActivo = ordenBloqueoPadreActiva,
                        routineViewModel = routineViewModel,
                        aiViewModel = aiViewModel,
                        settingsViewModel = settingsViewModel,
                        isPremiumListo = isPremiumUser != null,
                        onCambiarEstadoSistema = ::gestionarRestriccionesSistema,
                        onRouteChanged = { route ->
                            currentRoute = route
                            if (pendingLanguage.isNotEmpty() && esRutaSeguraParaIdioma(route)) {
                                aplicarLocale(pendingLanguage)
                                pendingLanguage = ""
                            }
                        }
                    )
                }
            }
        }
    }

    private fun evaluarYAplicarIdioma(idiomaCode: String) {
        if (esRutaSeguraParaIdioma(currentRoute)) {
            aplicarLocale(idiomaCode)
        } else {
            pendingLanguage = idiomaCode
        }
    }

    private fun esRutaSeguraParaIdioma(route: String): Boolean {
        if (route.isEmpty()) return true
        val esTareaActiva = route.contains(Routes.CHILD_TASK_EXECUTION_BASE) || route.contains(Routes.CHILD_TASK_FEEDBACK_BASE)
        return !esTareaActiva
    }

    private fun aplicarLocale(idiomaCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(idiomaCode)
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales != appLocale) {
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        // Guardar idioma para los widgets
        try {
            val widgetPrefs = getSharedPreferences("WIDGET_PREFS", Context.MODE_PRIVATE)
            widgetPrefs.edit().putString("app_language", idiomaCode).apply()

            val sessionPrefs = getSharedPreferences("SESSION_WIDGET_PREFS", Context.MODE_PRIVATE)
            sessionPrefs.edit().putString("app_language", idiomaCode).apply()

            // Notificar a los widgets para que se redibujen con el nuevo idioma
            com.example.upad.widget.ParentRoutineWidgetProvider.notificarCambioDatos(this)
            com.example.upad.widget.PremiumProgressWidgetProvider.notificarCambioDatos(this)

            val intent = Intent(this, com.example.upad.widget.ChildSessionMonitorWidgetProvider::class.java).apply {
                action = com.example.upad.widget.ParentRoutineWidgetProvider.ACTION_DATA_CHANGED
            }
            sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error en aplicarLocale", e)
        }
    }

    private fun gestionarRestriccionesSistema(activarFijacionKiosco: Boolean) {
        if (activarFijacionKiosco) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                        )
            }
            try { startLockTask() } catch (e: Exception) { Log.e(TAG, "Error en gestionarRestriccionesSistema", e) }
        } else {
            try {
                stopLockTask()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } catch (e: Exception) { Log.e(TAG, "Error en gestionarRestriccionesSistema", e) }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    private fun iniciarEscuchaNotificacionesPadre(userId: String) {
        notificationsJob?.cancel()
        if (userId.isEmpty()) return

        notificationsJob = lifecycleScope.launch {
            container.parentProfileRepository.observeAddedNotifications(userId)
                .catch { error -> Log.e(TAG, "Error al escuchar los avisos del tutor", error) }
                .collect { aviso ->
                    // Solo se notifican los avisos creados después de abrir la app.
                    val creadoEn = aviso.timestamp?.time ?: 0L
                    if (creadoEn > listenerStartTime - 5000) {
                        notificationHelper.mostrar(aviso.title, aviso.description)
                    }
                }
        }
    }

    private fun iniciarEscuchaPremiumPadre(userId: String, settingsViewModel: SettingsViewModel) {
        parentPremiumJob?.cancel()
        if (userId.isEmpty()) return

        parentPremiumJob = lifecycleScope.launch {
            container.parentProfileRepository.observePremium(userId)
                .catch { error -> Log.e(TAG, "Error al escuchar el plan del tutor", error) }
                .collect { isPremium -> settingsViewModel.setPremiumUser(isPremium) }
        }
    }

}

// ── DESTINO INICIAL ──────────────────────────────────────────────────────────
private fun obtenerDestinoInicial(context: android.content.Context, session: SessionRepository): String {
    val sharedPreferences = context.getSharedPreferences("UPadPrefs", android.content.Context.MODE_PRIVATE)
    val usuarioLogueado = session.currentUser != null
    val rolGuardado = sharedPreferences.getString("rol_usuario", null)
    return if (usuarioLogueado && !rolGuardado.isNullOrEmpty()) {
        if (rolGuardado == "padre" || rolGuardado == "padre_directo") Routes.PARENT_DASHBOARD else Routes.CHILD_START
    } else {
        Routes.ROLE_SELECTION
    }
}


// ── NAVEGACIÓN ───────────────────────────────────────────────────────────────
@Composable
fun UPadNavigation(
    bloqueoActivo: Boolean,
    routineViewModel: RoutineViewModel,
    aiViewModel: AiViewModel,
    settingsViewModel: SettingsViewModel,
    isPremiumListo: Boolean,
    onCambiarEstadoSistema: (Boolean) -> Unit,
    onRouteChanged: (String) -> Unit
) {
    val navController = rememberNavController()
    val session = LocalAppContainer.current.sessionRepository
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

    var padreIdDelHijo by remember { mutableStateOf("") }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route ?: ""
    val estaEnSeccionNiño = rutaActual.startsWith(Routes.CHILD_PREFIX)

    LaunchedEffect(rutaActual) { onRouteChanged(rutaActual) }

    LaunchedEffect(bloqueoActivo, estaEnSeccionNiño) {
        if (bloqueoActivo && estaEnSeccionNiño) onCambiarEstadoSistema(true)
        else onCambiarEstadoSistema(false)
    }

    val view = LocalView.current
    if (!view.isInEditMode && !(bloqueoActivo && estaEnSeccionNiño)) {
        val activity = view.context as Activity
        val window = activity.window
        window.statusBarColor = if (isDarkMode) android.graphics.Color.parseColor("#1E1E1E")
        else android.graphics.Color.TRANSPARENT
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = !isDarkMode
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            UPADBottomBar(navController = navController, settingsViewModel = settingsViewModel)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val contextoNav = androidx.compose.ui.platform.LocalContext.current

            NavHost(navController = navController, startDestination = Routes.SINCRONIZANDO) {

                // ── SINCRONIZANDO ────────────────────────────────────────────
                composable(Routes.SINCRONIZANDO) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    if (isPremiumListo) {
                        LaunchedEffect(Unit) {
                            val destinoReal = obtenerDestinoInicial(contextoNav, session)
                            navController.navigate(destinoReal) {
                                popUpTo(Routes.SINCRONIZANDO) { inclusive = true }
                            }
                        }
                    }
                }

                // ── AUTH ─────────────────────────────────────────────────────
                composable(Routes.ROLE_SELECTION) {
                    val contextoRol = androidx.compose.ui.platform.LocalContext.current
                    RoleSelectionScreen(
                        onRoleSelected = { role: String ->
                            val sharedPrefs = contextoRol.getSharedPreferences("UPadPrefs", android.content.Context.MODE_PRIVATE)
                            when (role) {
                                "padre_directo" -> {
                                    sharedPrefs.edit().putString("rol_usuario", "padre_directo").apply()
                                    navController.navigate(Routes.PARENT_DASHBOARD) {
                                        popUpTo(Routes.ROLE_SELECTION) { inclusive = true }
                                    }
                                }
                                "padre" -> {
                                    sharedPrefs.edit().putString("rol_usuario", "padre").apply()
                                    navController.navigate(Routes.WELCOME)
                                }
                                else -> {
                                    sharedPrefs.edit().putString("rol_usuario", "hijo").apply()
                                    navController.navigate(Routes.CHILD_START)
                                }
                            }
                        }
                    )
                }

                composable(Routes.WELCOME) {
                    WelcomeScreen(
                        onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                        onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                        onLoginExitoso = {
                            navController.navigate(Routes.PARENT_DASHBOARD) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLoginClick = { _, _ ->
                            navController.navigate(Routes.PARENT_DASHBOARD) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onRegisterComplete = { _, _ -> navController.navigate(Routes.SUBSCRIPTION_PLANS) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Routes.FORGOT_PASSWORD) {
                    ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
                }

                // ── SETUP ────────────────────────────────────────────────────
                composable(Routes.SUBSCRIPTION_PLANS) {
                    SubscriptionPlansScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateToBenefits = { navController.navigate(Routes.CHANGE_PLAN) },
                        onDirectToProfile = { navController.navigate(Routes.TRIAL_DISCLAIMER) }
                    )
                }

                composable(Routes.TRIAL_DISCLAIMER) {
                    TrialDisclaimerScreen(
                        onStartTrialClick = { navController.navigate(Routes.CHILD_PROFILE_SETUP) },
                        onMoreInfoClick = {}
                    )
                }

                composable(Routes.CHILD_PROFILE_SETUP) {
                    ChildProfileSetupScreen(
                        onBackClick = { navController.popBackStack() },
                        onSaveClick = { navController.navigate(Routes.EXPERIENCE_SETUP) }
                    )
                }

                composable(Routes.EXPERIENCE_SETUP) {
                    ExperienceSetupScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onBackClick = { navController.popBackStack() },
                        onNextClick = { navController.navigate(Routes.DEVICE_PAIRING) }
                    )
                }

                composable(Routes.DEVICE_PAIRING) {
                    DevicePairingScreen(
                        onNavigateToDashboard = { navController.navigate(Routes.PARENT_DASHBOARD) }
                    )
                }

                composable(Routes.TUTORIALS) {
                    TutorialsScreen(
                        onBackClick = { navController.popBackStack() },
                        onFinishSetupClick = { navController.navigate(Routes.PARENT_DASHBOARD) }
                    )
                }

                composable(Routes.CHANGE_PLAN) {
                    ChangePlanScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToPayment = { navController.navigate(Routes.PAYMENT_VIEW) }
                    )
                }

                composable(Routes.PAYMENT_VIEW) {
                    PaymentViewScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onPaymentConfirmed = {
                            val uid = session.currentUserId
                            settingsViewModel.setSuscripcionManual(true, uid)
                            navController.navigate(Routes.PARENT_DASHBOARD) {
                                popUpTo(Routes.PARENT_DASHBOARD) { inclusive = true }
                            }
                        }
                    )
                }

                // ── DASHBOARD PADRE ──────────────────────────────────────────
                composable(Routes.PARENT_DASHBOARD) {
                    LaunchedEffect(Unit) {
                        val uid = session.currentUserId
                        routineViewModel.cargarRutinasDesdeFirebase(uid)
                    }
                    RoutineDashboardScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateToCreateRoutine = { turn, day -> navController.navigate(Routes.createRoutine(turn, day)) },
                        onRoutineClick = { turn, day -> navController.navigate(Routes.createRoutine(turn, day)) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                        onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                        onNavigateToConnection = { navController.navigate(Routes.CONNECTION_CODE) },
                        onNavigateToAnalytics = { navController.navigate(Routes.ANALYTICS) },
                        onNavigateToDeviceManagement = { navController.navigate(Routes.DEVICE_MANAGEMENT) },
                        onNavigateToChangePlan = { navController.navigate(Routes.CHANGE_PLAN) },
                        onNavigateToTracking = { hijoId -> navController.navigate(Routes.tracking(hijoId)) },
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) }
                    )
                }

                // ✅ FIX: help_tutorial ahora pasa onNavigateToHelp y onNavigateToProfile
                composable(Routes.HELP_TUTORIAL) {
                    HelpTutorialScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
                    )
                }

                composable(Routes.TODAY_CALENDAR) {
                    TodayCalendarScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
                    )
                }

                composable(Routes.EMOTIONS_TRACK) {
                    EmotionsTrackScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
                    )
                }

                composable(Routes.NOTIFICATIONS) {
                    NotificationsScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
                    )
                }

                composable(
                    route = Routes.TRACKING,
                    arguments = listOf(navArgument(Routes.ARG_HIJO_ID) { type = NavType.StringType })
                ) { backStackEntry ->
                    val hijoId = backStackEntry.arguments?.getString(Routes.ARG_HIJO_ID) ?: ""
                    val trackingViewModel: com.example.upad.viewmodel.TrackingViewModel = viewModel(
                        factory = AppViewModelFactory(LocalAppContainer.current)
                    )
                    HijoTrackingScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        hijoId = hijoId,
                        trackingViewModel = trackingViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
                    )
                }

                // ✅ FIX: profile ahora recibe onNavigateToHelp
                composable(Routes.PROFILE) {
                    val contextoPerfil = androidx.compose.ui.platform.LocalContext.current
                    ProfileScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onLogoutSuccess = {
                            val sharedPrefs = contextoPerfil.getSharedPreferences("UPadPrefs", android.content.Context.MODE_PRIVATE)
                            sharedPrefs.edit()
                                .remove("rol_usuario")
                                .remove("pref_tema_oscuro")
                                .apply()
                            navController.navigate(Routes.ROLE_SELECTION) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        // ✅ FIX: conectado — antes faltaba este parámetro
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) }
                    )
                }

                // ✅ FIX: settings recibe onNavigateToHelp si tu SettingsScreen lo tiene
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
                    )
                }

                composable(Routes.CONNECTION_CODE) {
                    ConnectionScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onLinkSuccess = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
                    )
                }

                composable(Routes.ANALYTICS) {
                    AnalyticsScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        aiViewModel = aiViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToPremium = { navController.navigate(Routes.SUBSCRIPTION_PLANS) },
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
                    )
                }
                composable(Routes.DEVICE_MANAGEMENT) {
                    DeviceManagementScreen(
                        routineViewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate(Routes.HELP_TUTORIAL) },
                        onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
                    )
                }

                // ── CREATE ROUTINE ───────────────────────────────────────────
                composable(
                    route = Routes.CREATE_ROUTINE,
                    arguments = listOf(
                        navArgument(Routes.ARG_ROUTINE_TURN) { type = NavType.StringType },
                        navArgument(Routes.ARG_SELECTED_DAY) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val turn = backStackEntry.arguments?.getString(Routes.ARG_ROUTINE_TURN) ?: "MAÑANA"
                    val day = backStackEntry.arguments?.getString(Routes.ARG_SELECTED_DAY) ?: "LUNES"
                    val turnoNormalizado = when (turn.uppercase().trim()) {
                        "MAÑANA", "MANANA" -> "MAÑANA"
                        "TARDE" -> "TARDE"
                        "NOCHE" -> "NOCHE"
                        else -> "MAÑANA"
                    }
                    val diaNormalizado = when (day.uppercase().trim()) {
                        "LUNES", "LUN" -> "LUNES"
                        "MARTES", "MAR" -> "MARTES"
                        "MIERCOLES", "MIE", "MIÉRCOLES" -> "MIERCOLES"
                        "JUEVES", "JUE" -> "JUEVES"
                        "VIERNES", "VIE" -> "VIERNES"
                        "SABADO", "SAB", "SÁBADO" -> "SABADO"
                        "DOMINGO", "DOM" -> "DOMINGO"
                        else -> "LUNES"
                    }

                    LaunchedEffect(turnoNormalizado, diaNormalizado) {
                        val uid = session.currentUserId
                        routineViewModel.cargarRutinasPorDia(uid, diaNormalizado)
                    }

                    val listaPasosTurno by when (turnoNormalizado) {
                        "MAÑANA" -> routineViewModel.tasksManana.collectAsState()
                        "TARDE" -> routineViewModel.tasksTarde.collectAsState()
                        else -> routineViewModel.tasksNoche.collectAsState()
                    }

                    CreateRoutineScreen(
                        routineTurn = turnoNormalizado,
                        childName = "tu hijo",
                        pasosSeleccionados = listaPasosTurno,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToPictogramSearch = { navController.navigate(Routes.pictogramSelection(turnoNormalizado, diaNormalizado)) },
                        onRemoveTaskClick = { index -> routineViewModel.removeTask(turnoNormalizado, index) },
                        onSendRoutine = {
                            val uid = session.currentUserId
                            routineViewModel.saveAll(uid, turnoNormalizado)
                            navController.navigate(Routes.PARENT_DASHBOARD)
                        },
                        viewModel = routineViewModel,
                        settingsViewModel = settingsViewModel,
                        aiViewModel = aiViewModel,
                        drawableId = android.R.drawable.ic_menu_manage,
                        diaInicial = diaNormalizado,
                        // ✅ FIX: conectado
                    )
                }

                // ── PICTOGRAM ────────────────────────────────────────────────
                composable(
                    route = Routes.PICTOGRAM_SELECTION,
                    arguments = listOf(
                        navArgument(Routes.ARG_ROUTINE_TURN) { type = NavType.StringType },
                        navArgument(Routes.ARG_SELECTED_DAY) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val turn = backStackEntry.arguments?.getString(Routes.ARG_ROUTINE_TURN) ?: "MAÑANA"
                    val day = backStackEntry.arguments?.getString(Routes.ARG_SELECTED_DAY) ?: "LUNES"
                    val turnoNormalizado = when (turn.uppercase().trim()) {
                        "MAÑANA", "MANANA" -> "MAÑANA"; "TARDE" -> "TARDE"; "NOCHE" -> "NOCHE"; else -> "MAÑANA"
                    }
                    val diaNormalizado = when (day.uppercase().trim()) {
                        "LUNES", "LUN" -> "LUNES"; "MARTES", "MAR" -> "MARTES"
                        "MIERCOLES", "MIE", "MIÉRCOLES" -> "MIERCOLES"; "JUEVES", "JUE" -> "JUEVES"
                        "VIERNES", "VIE" -> "VIERNES"; "SABADO", "SAB", "SÁBADO" -> "SABADO"
                        "DOMINGO", "DOM" -> "DOMINGO"; else -> "LUNES"
                    }
                    PictogramSelectionScreen(
                        viewModel = routineViewModel,
                        onBackClick = { navController.popBackStack() },
                        onPictogramSelected = { description, url ->
                            val uid = session.currentUserId
                            routineViewModel.addTaskConDia(turnoNormalizado, description, url, diaNormalizado, uid)
                            navController.popBackStack()
                        }
                    )
                }

                // ── SECCIÓN NIÑO ─────────────────────────────────────────────
                composable(Routes.CHILD_START) {
                    ChildStartScreen(
                        routineViewModel = routineViewModel,
                        onNavigateToTask = { actividadNombre, turno ->
                            navController.navigate(Routes.childTaskExecution(actividadNombre, turno))
                        },
                        onNavigateToCompleted = { navController.navigate(Routes.CHILD_ROUTINE_COMPLETED) },
                        onPadreIdObtenido = { pId -> padreIdDelHijo = pId }
                    )
                }

                composable(
                    route = Routes.CHILD_TASK_EXECUTION,
                    arguments = listOf(
                        navArgument(Routes.ARG_ACTIVITY_NAME) { type = NavType.StringType },
                        navArgument(Routes.ARG_ROUTINE_TURN) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val activityName = backStackEntry.arguments?.getString(Routes.ARG_ACTIVITY_NAME) ?: "Actividad"
                    val turn = backStackEntry.arguments?.getString(Routes.ARG_ROUTINE_TURN) ?: "MAÑANA"
                    TaskExecutionScreen(
                        viewModel = routineViewModel,
                        activityName = activityName,
                        turn = turn,
                        padreId = padreIdDelHijo,
                        onFinishRoutine = { nombreDeLaTareaCompletada ->
                            navController.navigate(Routes.childTaskFeedback(nombreDeLaTareaCompletada))
                        }
                    )
                }

                composable(
                    route = Routes.CHILD_TASK_FEEDBACK,
                    arguments = listOf(navArgument(Routes.ARG_ACTIVITY_NAME) { type = NavType.StringType })
                ) { backStackEntry ->
                    val activityName = backStackEntry.arguments?.getString(Routes.ARG_ACTIVITY_NAME) ?: "Actividad"
                    val context = androidx.compose.ui.platform.LocalContext.current
                    TaskFeedbackScreen(
                        activityName = activityName,
                        onFeedbackSelected = { emocion ->
                            val idPadre = padreIdDelHijo.ifEmpty {
                                session.currentUserId
                            }
                            val horaActual = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                            val turnoActual = when {
                                horaActual < 13 -> "MAÑANA"
                                horaActual in 13..17 -> "TARDE"
                                else -> "NOCHE"
                            }
                            routineViewModel.registrarFeedbackEmocional(
                                userId = idPadre,
                                turn = turnoActual,
                                actividadNombre = activityName,
                                emocionSeleccionada = emocion,
                                context = context
                            )
                            routineViewModel.cargarMensajesRefuerzo(idPadre, turnoActual)
                            navController.navigate(Routes.CHILD_ROUTINE_COMPLETED) {
                                popUpTo(Routes.CHILD_START) { inclusive = false }
                            }
                        }
                    )
                }

                composable(Routes.CHILD_ROUTINE_COMPLETED) {
                    val reinforcementMessages by routineViewModel.reinforcementMessages.collectAsState()
                    RoutineCompletedScreen(
                        nextActivityPreview = "",
                        reinforcementMessages = reinforcementMessages,
                        onFinishClick = {
                            navController.navigate(Routes.CHILD_START) {
                                popUpTo(Routes.CHILD_START) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.ACHIEVEMENT_REPORT) {
                    AchievementReportScreen(
                        onBackClick = { navController.popBackStack() },
                        onViewDetailsClick = { navController.navigate(Routes.ACTIVITY_DETAILS) }
                    )
                }

                composable(Routes.ACTIVITY_DETAILS) {
                    LaunchedEffect(Unit) {
                        val uid = session.currentUserId
                        routineViewModel.cargarRutinasDesdeFirebase(uid)
                    }
                    ActivityDetailsScreen(
                        viewModel = routineViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}