package com.example.upad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Importaciones de tus pantallas
import com.example.upad.child.TaskExecutionScreen
import com.example.upad.auth.ForgotPasswordScreen
import com.example.upad.auth.LoginScreen
import com.example.upad.auth.RegisterScreen
import com.example.upad.auth.RoleSelectionScreen
import com.example.upad.auth.WelcomeScreen
import com.example.upad.child.ChildStartScreen
import com.example.upad.child.RoutineCompletedScreen
import com.example.upad.child.TaskFeedbackScreen
import com.example.upad.dashboard.AchievementReportScreen
import com.example.upad.dashboard.ActivityDetailsScreen
import com.example.upad.dashboard.NotificationsScreen
import com.example.upad.dashboard.RoutineDashboardScreen
import com.example.upad.data.FirebaseRepository
import com.example.upad.routines.CreateRoutineScreen
import com.example.upad.routines.PictogramSelectionScreen
import com.example.upad.setup.ChildProfileSetupScreen
import com.example.upad.setup.DevicePairingScreen
import com.example.upad.setup.ExperienceSetupScreen
import com.example.upad.setup.SubscriptionPlansScreen
import com.example.upad.setup.TrialDisclaimerScreen
import com.example.upad.setup.TutorialsScreen

import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.RoutineViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            // CAMBIO CLAVE: Llamamos a la navegación principal, no solo a una pantalla suelta
            UPadNavigation()
        }
    }
}

@Composable
fun UPadNavigation() {
    val navController = rememberNavController()

    // 1. Inicializamos el Repositorio y el ViewModel
    // Nota: Si no tienes una Factory, puedes instanciarlo simple por ahora:
    val repository = remember { FirebaseRepository() }
    val routineViewModel: RoutineViewModel = viewModel(
        factory = com.example.upad.viewmodel.RoutineViewModelFactory(repository)
    )

    // Observamos las tareas para pasarlas a la pantalla de creación
    val tareasPorEnviar by routineViewModel.tasks.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "role_selection"
    ) {
        // --- SECCIÓN AUTENTICACIÓN Y ROLES (Sin cambios) ---
        composable("role_selection") {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    if (role == "padre") navController.navigate("welcome")
                    else navController.navigate("child_start")
                }
            )
        }

        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegister = { navController.navigate("register") },
                onGoogleSignInClick = { println("Google Login") }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginClick = { _, _ -> navController.navigate("parent_dashboard") },
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

        // --- SECCIÓN CONFIGURACIÓN (SETUP) ---
        composable("subscription_plans") {
            SubscriptionPlansScreen(
                onPlanSelected = { planType ->
                    if (planType == "premium") navController.navigate("child_profile_setup")
                    else navController.navigate("trial_disclaimer")
                },
                onSkip = { navController.navigate("trial_disclaimer") }
            )
        }

        composable("trial_disclaimer") {
            TrialDisclaimerScreen(
                onStartTrialClick = { navController.navigate("child_profile_setup") },
                onMoreInfoClick = { }
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
                onBackClick = { navController.popBackStack() },
                onNextClick = { navController.navigate("device_pairing") }
            )
        }

        composable("device_pairing") {
            DevicePairingScreen(
                onDeviceSelected = { navController.navigate("tutorials") },
                onNavigateToDashboard = { navController.navigate("parent_dashboard") }
            )
        }

        composable("tutorials") {
            TutorialsScreen(
                onBackClick = { navController.popBackStack() },
                onFinishSetupClick = { navController.navigate("parent_dashboard") }
            )
        }

        // --- SECCIÓN PADRE (DASHBOARD Y RUTINAS) ---
        composable("parent_dashboard") {
            RoutineDashboardScreen(
                onNavigateToCreateRoutine = { navController.navigate("create_routine/Nuevo") },
                onRoutineClick = { routineName ->
                    routineViewModel.updateName(routineName)
                    navController.navigate("create_routine/$routineName")
                }
            )
        }

        composable(
            route = "create_routine/{routineTurn}",
            arguments = listOf(navArgument("routineTurn") { type = NavType.StringType })
        ) { backStackEntry ->
            val turn = backStackEntry.arguments?.getString("routineTurn") ?: "Mañana"

            CreateRoutineScreen(
                routineTurn = turn,
                childName = "Mateo",
                // Ahora usamos las descripciones de las tareas del ViewModel
                // pasosSeleccionados = tareasPorEnviar.map { it.description },
                // AHORA
                pasosSeleccionados = tareasPorEnviar,
                onBackClick = { navController.popBackStack() },
                onNavigateToPictogramSearch = { navController.navigate("pictogram_selection") },
                onSendRoutine = {
                    // Aquí llamamos a la función real de Firebase
                    routineViewModel.saveAll("ID_DEL_PADRE_AQUI")
                    navController.navigate("parent_dashboard")
                },
                drawableId = R.drawable.logo_upad
            )
        }

        composable("pictogram_selection") {
            PictogramSelectionScreen(
                viewModel = routineViewModel,
                onBackClick = { navController.popBackStack() },
                onPictogramSelected = { nombre, url ->
                    // Agregamos la tarea real con su imagen de ARASAAC
                    routineViewModel.addTask(nombre, url)
                    navController.popBackStack()
                }
            )
        }

        // --- SECCIÓN NIÑO (EJECUCIÓN DE TAREAS) ---
        composable("child_start") {
            ChildStartScreen(onStartRoutineClick = { navController.navigate("child_task_execution") })
        }

        composable("child_task_execution") {
            var currentStep by remember { mutableIntStateOf(0) }
            val pasosBano = listOf(
                Pair("ME QUITO LA ROPA Y ENTRO EN LA DUCHA", R.drawable.paso_1),
                Pair("ME ENJABONO LA CABEZA Y EL CUERPO", R.drawable.paso_2),
                Pair("ME ACLARO, SALGO, ME SECO Y VISTO", R.drawable.paso_3),
                Pair("ME SECO EL PELO, ME ECHO COLONIA Y LISTO!", R.drawable.paso_4)
            )

            TaskExecutionScreen(
                activityNumber = 1,
                activityName = "BAÑARSE",
                stepNumber = currentStep + 1,
                stepName = pasosBano[currentStep].first,
                drawableId = pasosBano[currentStep].second,
                progress = (currentStep + 1).toFloat() / pasosBano.size,
                isLastStep = currentStep == pasosBano.size - 1,
                onNextStepClick = {
                    if (currentStep < pasosBano.size - 1) currentStep++
                    else navController.navigate("child_task_feedback")
                }
            )
        }

        composable("child_task_feedback") {
            TaskFeedbackScreen(onFeedbackSelected = { navController.navigate("child_routine_completed") })
        }

        composable("child_routine_completed") {
            RoutineCompletedScreen(
                onFinishClick = {
                    navController.navigate("child_start") { popUpTo("child_start") { inclusive = true } }
                }
            )
        }

        // --- SECCIÓN REPORTES ---
        composable("notifications") {
            NotificationsScreen(onViewReportClick = { navController.navigate("achievement_report") })
        }

        composable("achievement_report") {
            AchievementReportScreen(
                onBackClick = { navController.popBackStack() },
                onViewDetailsClick = { navController.navigate("activity_details") }
            )
        }

        composable("activity_details") {
            ActivityDetailsScreen(onBack = { navController.popBackStack() })
        }
    } // Aquí cierra el NavHost
} // Aquí cierra el UPadNavigation