package com.example.upad

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
import com.example.upad.data.FirebaseRepository
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
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.dashboard.ConnectionScreen
import com.example.upad.dashboard.AnalyticsScreen
import com.example.upad.premium.PaymentViewScreen
import android.content.Context
import android.content.Intent
import com.example.upad.components.UPADBottomBar
import com.example.upad.dashboard.EmotionsTrackScreen
import com.example.upad.dashboard.HelpTutorialScreen
import com.example.upad.dashboard.TodayCalendarScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private var currentRoute = ""
    private var pendingLanguage = ""

    private var ordenBloqueoPadreActiva by mutableStateOf(false)
    private val firestore = FirebaseFirestore.getInstance()
    private var kioscoListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null
    private var parentPremiumListener: ListenerRegistration? = null
    private val listenerStartTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        requestNotificationPermission()

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        kioscoListener = firestore.collection("dispositivos_niños").document(deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val kioscoActivo = snapshot.getBoolean("kioscoActivo") ?: false
                    ordenBloqueoPadreActiva = kioscoActivo
                } else {
                    ordenBloqueoPadreActiva = false
                }
            }

        setContent {
            val repository = remember { FirebaseRepository() }
            val routineViewModel: RoutineViewModel = viewModel(
                factory = com.example.upad.viewmodel.RoutineViewModelFactory(repository)
            )

            LaunchedEffect(routineViewModel) {
                FirebaseAuth.getInstance().addAuthStateListener { auth ->
                    val user = auth.currentUser
                    if (user != null) {
                        val sharedPrefs = getSharedPreferences("UPadPrefs", Context.MODE_PRIVATE)
                        val rol = sharedPrefs.getString("rol_usuario", null)
                        if (rol == "padre" || rol == "padre_directo") {
                            iniciarEscuchaNotificacionesPadre(user.uid)
                            iniciarEscuchaPremiumPadre(user.uid, routineViewModel)
                        }
                    } else {
                        notificationsListener?.remove()
                        parentPremiumListener?.remove()
                    }
                }
            }

            val appLanguage by routineViewModel.appLanguage.collectAsState()
            LaunchedEffect(appLanguage) {
                evaluarYAplicarIdioma(appLanguage)
            }

            val sharedPrefs = remember { getSharedPreferences("UPadPrefs", Context.MODE_PRIVATE) }
            val sistemaEstaEnOscuro = isSystemInDarkTheme()

            val isDarkModeFlow by routineViewModel.isDarkMode.collectAsState()
            var estadoTemaOscuroReal by remember {
                mutableStateOf(sharedPrefs.getBoolean("pref_tema_oscuro", sistemaEstaEnOscuro))
            }

            LaunchedEffect(isDarkModeFlow) {
                estadoTemaOscuroReal = isDarkModeFlow
                sharedPrefs.edit().putBoolean("pref_tema_oscuro", isDarkModeFlow).apply()
            }

            val isPremiumUser by routineViewModel.isUserPremium.collectAsState(initial = null)

            UPadTheme(darkTheme = estadoTemaOscuroReal, isPremium = isPremiumUser ?: false) {
                UPadNavigation(
                    bloqueoActivo = ordenBloqueoPadreActiva,
                    routineViewModel = routineViewModel,
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

    private fun evaluarYAplicarIdioma(idiomaCode: String) {
        if (esRutaSeguraParaIdioma(currentRoute)) {
            aplicarLocale(idiomaCode)
        } else {
            pendingLanguage = idiomaCode
        }
    }

    private fun esRutaSeguraParaIdioma(route: String): Boolean {
        if (route.isEmpty()) return true
        val esTareaActiva = route.contains("child_task_execution") || route.contains("child_task_feedback")
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
            e.printStackTrace()
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
            try { startLockTask() } catch (e: Exception) { e.printStackTrace() }
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
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones de Rutina"
            val descriptionText = "Alertas sobre el estado de las rutinas del niño"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("upad_notifications_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
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
        notificationsListener?.remove()
        if (userId.isEmpty() || userId == "PADRE_TEST") return

        notificationsListener = firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    for (dc in snapshot.documentChanges) {
                        if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val doc = dc.document
                            val timestamp = doc.getTimestamp("timestamp")
                            val tMillis = timestamp?.toDate()?.time ?: 0L
                            if (tMillis > listenerStartTime - 5000) {
                                val title = doc.getString("title") ?: "U-Pad Alerta"
                                val description = doc.getString("description") ?: ""
                                mostrarNotificacionLocal(title, description)
                            }
                        }
                    }
                }
            }
    }

    private fun mostrarNotificacionLocal(title: String, description: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                android.app.PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val builder = androidx.core.app.NotificationCompat.Builder(this, "upad_notifications_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(androidx.core.app.NotificationManagerCompat.from(this)) {
            try {
                val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

                if (hasPermission) {
                    notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    private fun iniciarEscuchaPremiumPadre(userId: String, routineViewModel: RoutineViewModel) {
        parentPremiumListener?.remove()
        if (userId.isEmpty() || userId == "PADRE_TEST") return

        parentPremiumListener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val isPremium = snapshot.getBoolean("isPremium") ?: false
                    routineViewModel.setPremiumUser(isPremium)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        kioscoListener?.remove()
        notificationsListener?.remove()
        parentPremiumListener?.remove()
    }

    val ROUTE_HELP = "help_tutorial"
}

// ── DESTINO INICIAL ──────────────────────────────────────────────────────────
private fun obtenerDestinoInicial(context: android.content.Context): String {
    val auth = FirebaseAuth.getInstance()
    val sharedPreferences = context.getSharedPreferences("UPadPrefs", android.content.Context.MODE_PRIVATE)
    val usuarioLogueado = auth.currentUser != null
    val rolGuardado = sharedPreferences.getString("rol_usuario", null)
    return if (usuarioLogueado && !rolGuardado.isNullOrEmpty()) {
        if (rolGuardado == "padre" || rolGuardado == "padre_directo") "parent_dashboard" else "child_start"
    } else {
        "role_selection"
    }
}


// ── NAVEGACIÓN ───────────────────────────────────────────────────────────────
@Composable
fun UPadNavigation(
    bloqueoActivo: Boolean,
    routineViewModel: RoutineViewModel,
    isPremiumListo: Boolean,
    onCambiarEstadoSistema: (Boolean) -> Unit,
    onRouteChanged: (String) -> Unit
) {
    val navController = rememberNavController()
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()

    var padreIdDelHijo by remember { mutableStateOf("") }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route ?: ""
    val estaEnSeccionNiño = rutaActual.startsWith("child_")

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
            UPADBottomBar(navController = navController, routineViewModel = routineViewModel)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val contextoNav = androidx.compose.ui.platform.LocalContext.current

            NavHost(navController = navController, startDestination = "sincronizando") {

                // ── SINCRONIZANDO ────────────────────────────────────────────
                composable("sincronizando") {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    if (isPremiumListo) {
                        LaunchedEffect(Unit) {
                            val destinoReal = obtenerDestinoInicial(contextoNav)
                            navController.navigate(destinoReal) {
                                popUpTo("sincronizando") { inclusive = true }
                            }
                        }
                    }
                }

                // ── AUTH ─────────────────────────────────────────────────────
                composable("role_selection") {
                    val contextoRol = androidx.compose.ui.platform.LocalContext.current
                    RoleSelectionScreen(
                        onRoleSelected = { role: String ->
                            val sharedPrefs = contextoRol.getSharedPreferences("UPadPrefs", android.content.Context.MODE_PRIVATE)
                            when (role) {
                                "padre_directo" -> {
                                    sharedPrefs.edit().putString("rol_usuario", "padre_directo").apply()
                                    navController.navigate("parent_dashboard") {
                                        popUpTo("role_selection") { inclusive = true }
                                    }
                                }
                                "padre" -> {
                                    sharedPrefs.edit().putString("rol_usuario", "padre").apply()
                                    navController.navigate("welcome")
                                }
                                else -> {
                                    sharedPrefs.edit().putString("rol_usuario", "hijo").apply()
                                    navController.navigate("child_start")
                                }
                            }
                        }
                    )
                }

                composable("welcome") {
                    WelcomeScreen(
                        onNavigateToLogin = { navController.navigate("login") },
                        onNavigateToRegister = { navController.navigate("register") },
                        onLoginExitoso = {
                            navController.navigate("parent_dashboard") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                    )
                }

                composable("login") {
                    LoginScreen(
                        onLoginClick = { _, _ ->
                            navController.navigate("parent_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onForgotPasswordClick = { navController.navigate("forgot_password") },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onRegisterComplete = { _, _ -> navController.navigate("subscription_plans") },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("forgot_password") {
                    ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
                }

                // ── SETUP ────────────────────────────────────────────────────
                composable("subscription_plans") {
                    SubscriptionPlansScreen(
                        routineViewModel = routineViewModel,
                        onNavigateToBenefits = { navController.navigate("change_plan") },
                        onDirectToProfile = { navController.navigate("trial_disclaimer") }
                    )
                }

                composable("trial_disclaimer") {
                    TrialDisclaimerScreen(
                        onStartTrialClick = { navController.navigate("child_profile_setup") },
                        onMoreInfoClick = {}
                    )
                }

                composable("child_profile_setup") {
                    ChildProfileSetupScreen(
                        onBackClick = { navController.popBackStack() },
                        onSaveClick = { navController.navigate("experience_setup") }
                    )
                }

                composable("experience_setup") {
                    ExperienceSetupScreen(
                        routineViewModel = routineViewModel,
                        onBackClick = { navController.popBackStack() },
                        onNextClick = { navController.navigate("device_pairing") }
                    )
                }

                composable("device_pairing") {
                    DevicePairingScreen(
                        onNavigateToDashboard = { navController.navigate("parent_dashboard") }
                    )
                }

                composable("tutorials") {
                    TutorialsScreen(
                        onBackClick = { navController.popBackStack() },
                        onFinishSetupClick = { navController.navigate("parent_dashboard") }
                    )
                }

                composable("change_plan") {
                    ChangePlanScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToPayment = { navController.navigate("payment_view") }
                    )
                }

                composable("payment_view") {
                    PaymentViewScreen(
                        routineViewModel = routineViewModel,
                        onPaymentConfirmed = {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            routineViewModel.setSuscripcionManual(true, uid)
                            navController.navigate("parent_dashboard") {
                                popUpTo("parent_dashboard") { inclusive = true }
                            }
                        }
                    )
                }

                // ── DASHBOARD PADRE ──────────────────────────────────────────
                composable("parent_dashboard") {
                    LaunchedEffect(Unit) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        routineViewModel.cargarRutinasDesdeFirebase(uid)
                    }
                    RoutineDashboardScreen(
                        routineViewModel = routineViewModel,
                        onNavigateToCreateRoutine = { turn, day -> navController.navigate("create_routine/$turn/$day") },
                        onRoutineClick = { turn, day -> navController.navigate("create_routine/$turn/$day") },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToConnection = { navController.navigate("connection_code") },
                        onNavigateToAnalytics = { navController.navigate("analytics") },
                        onNavigateToDeviceManagement = { navController.navigate("device_management") },
                        onNavigateToChangePlan = { navController.navigate("change_plan") },
                        onNavigateToTracking = { hijoId -> navController.navigate("tracking/$hijoId") },
                        onNavigateToHelp = { navController.navigate("help_tutorial") }
                    )
                }

                // ✅ FIX: help_tutorial ahora pasa onNavigateToHelp y onNavigateToProfile
                composable("help_tutorial") {
                    HelpTutorialScreen(
                        routineViewModel = routineViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate("help_tutorial") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }

                composable("today_calendar") {
                    TodayCalendarScreen(
                        routineViewModel = routineViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate("help_tutorial") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }

                composable("emotions_track") {
                    EmotionsTrackScreen(
                        routineViewModel = routineViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate("help_tutorial") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }

                composable("notifications") {
                    NotificationsScreen(
                        routineViewModel = routineViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate("help_tutorial") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }

                composable(
                    route = "tracking/{hijoId}",
                    arguments = listOf(navArgument("hijoId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val hijoId = backStackEntry.arguments?.getString("hijoId") ?: ""
                    val trackingViewModel: com.example.upad.viewmodel.TrackingViewModel = viewModel()
                    HijoTrackingScreen(
                        routineViewModel = routineViewModel,
                        hijoId = hijoId,
                        trackingViewModel = trackingViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate("help_tutorial") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }

                // ✅ FIX: profile ahora recibe onNavigateToHelp
                composable("profile") {
                    val contextoPerfil = androidx.compose.ui.platform.LocalContext.current
                    ProfileScreen(
                        routineViewModel = routineViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onLogoutSuccess = {
                            val sharedPrefs = contextoPerfil.getSharedPreferences("UPadPrefs", android.content.Context.MODE_PRIVATE)
                            sharedPrefs.edit()
                                .remove("rol_usuario")
                                .remove("pref_tema_oscuro")
                                .apply()
                            navController.navigate("role_selection") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        // ✅ FIX: conectado — antes faltaba este parámetro
                        onNavigateToHelp = { navController.navigate("help_tutorial") }
                    )
                }

                // ✅ FIX: settings recibe onNavigateToHelp si tu SettingsScreen lo tiene
                composable("settings") {
                    SettingsScreen(
                        routineViewModel = routineViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate("help_tutorial") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }

                composable("connection_code") {
                    ConnectionScreen(
                        routineViewModel = routineViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onLinkSuccess = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate("help_tutorial") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }

                composable("analytics") {
                    AnalyticsScreen(
                        routineViewModel = routineViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToPremium = { navController.navigate("subscription_plans") },
                        onNavigateToHelp = { navController.navigate("help_tutorial") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }
                composable("device_management") {
                    DeviceManagementScreen(
                        routineViewModel = routineViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate("help_tutorial") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }

                // ── CREATE ROUTINE ───────────────────────────────────────────
                composable(
                    route = "create_routine/{routineTurn}/{selectedDay}",
                    arguments = listOf(
                        navArgument("routineTurn") { type = NavType.StringType },
                        navArgument("selectedDay") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val turn = backStackEntry.arguments?.getString("routineTurn") ?: "MAÑANA"
                    val day = backStackEntry.arguments?.getString("selectedDay") ?: "LUNES"
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
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
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
                        onNavigateToPictogramSearch = { navController.navigate("pictogram_selection/$turnoNormalizado/$diaNormalizado") },
                        onRemoveTaskClick = { index -> routineViewModel.removeTask(turnoNormalizado, index) },
                        onSendRoutine = {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            routineViewModel.saveAll(uid, turnoNormalizado)
                            navController.navigate("parent_dashboard")
                        },
                        viewModel = routineViewModel,
                        drawableId = android.R.drawable.ic_menu_manage,
                        diaInicial = diaNormalizado,
                        // ✅ FIX: conectado
                    )
                }

                // ── PICTOGRAM ────────────────────────────────────────────────
                composable(
                    route = "pictogram_selection/{routineTurn}/{selectedDay}",
                    arguments = listOf(
                        navArgument("routineTurn") { type = NavType.StringType },
                        navArgument("selectedDay") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val turn = backStackEntry.arguments?.getString("routineTurn") ?: "MAÑANA"
                    val day = backStackEntry.arguments?.getString("selectedDay") ?: "LUNES"
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
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            routineViewModel.addTaskConDia(turnoNormalizado, description, url, diaNormalizado, uid)
                            navController.popBackStack()
                        }
                    )
                }

                // ── SECCIÓN NIÑO ─────────────────────────────────────────────
                composable("child_start") {
                    ChildStartScreen(
                        routineViewModel = routineViewModel,
                        onNavigateToTask = { actividadNombre, turno ->
                            navController.navigate("child_task_execution/$actividadNombre/$turno")
                        },
                        onNavigateToCompleted = { navController.navigate("child_routine_completed") },
                        onPadreIdObtenido = { pId -> padreIdDelHijo = pId }
                    )
                }

                composable(
                    route = "child_task_execution/{activityName}/{routineTurn}",
                    arguments = listOf(
                        navArgument("activityName") { type = NavType.StringType },
                        navArgument("routineTurn") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val activityName = backStackEntry.arguments?.getString("activityName") ?: "Actividad"
                    val turn = backStackEntry.arguments?.getString("routineTurn") ?: "MAÑANA"
                    TaskExecutionScreen(
                        viewModel = routineViewModel,
                        activityName = activityName,
                        turn = turn,
                        padreId = padreIdDelHijo,
                        onFinishRoutine = { nombreDeLaTareaCompletada ->
                            navController.navigate("child_task_feedback/$nombreDeLaTareaCompletada")
                        }
                    )
                }

                composable(
                    route = "child_task_feedback/{activityName}",
                    arguments = listOf(navArgument("activityName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val activityName = backStackEntry.arguments?.getString("activityName") ?: "Actividad"
                    val context = androidx.compose.ui.platform.LocalContext.current
                    TaskFeedbackScreen(
                        activityName = activityName,
                        onFeedbackSelected = { emocion ->
                            val idPadre = padreIdDelHijo.ifEmpty {
                                FirebaseAuth.getInstance().currentUser?.uid ?: "PADRE_TEST"
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
                            navController.navigate("child_routine_completed") {
                                popUpTo("child_start") { inclusive = false }
                            }
                        }
                    )
                }

                composable("child_routine_completed") {
                    val reinforcementMessages by routineViewModel.reinforcementMessages.collectAsState()
                    RoutineCompletedScreen(
                        nextActivityPreview = "",
                        reinforcementMessages = reinforcementMessages,
                        onFinishClick = {
                            navController.navigate("child_start") {
                                popUpTo("child_start") { inclusive = true }
                            }
                        }
                    )
                }

                composable("achievement_report") {
                    AchievementReportScreen(
                        onBackClick = { navController.popBackStack() },
                        onViewDetailsClick = { navController.navigate("activity_details") }
                    )
                }

                composable("activity_details") {
                    LaunchedEffect(Unit) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
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