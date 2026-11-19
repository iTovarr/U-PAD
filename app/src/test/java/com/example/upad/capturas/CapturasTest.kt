package com.example.upad.capturas

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import coil.Coil
import coil.ImageLoader
import com.example.upad.child.ChildTheme
import com.example.upad.child.RoutineCompletedScreen
import com.example.upad.child.TareaEnCurso
import com.example.upad.child.TareasDelDia
import com.example.upad.child.TaskFeedbackScreen
import com.example.upad.dashboard.SensoryProfileContent
import com.example.upad.dashboard.SettingsContent
import com.example.upad.data.SensoryProfile
import com.example.upad.data.TimerStyle
import com.example.upad.ui.theme.AppPalette
import com.example.upad.ui.theme.UPadTheme
import com.example.upad.utils.Turnos
import com.example.upad.viewmodel.TaskItem
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Genera las capturas de pantalla del README (docs/capturas) con datos de ejemplo.
 * Se ejecutan en la JVM con Robolectric, sin dispositivo ni Firebase.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "es-w400dp-h860dp-xhdpi")
class CapturasTest {

    @get:Rule
    val compose = createComposeRule()

    @Before
    fun cargarImagenesAlInstante() {
        // Coil carga los pictogramas en el hilo principal para que aparezcan en la captura.
        val contexto = ApplicationProvider.getApplicationContext<android.content.Context>()
        Coil.setImageLoader(ImageLoader.Builder(contexto).dispatcher(Dispatchers.Main.immediate).build())
    }

    private fun pictograma(nombre: String) = "android.resource://com.example.upad/drawable/$nombre"

    private val tareasManana = listOf(
        TaskItem(actividad = "Entrar en la ducha", imageUrl = pictograma("paso_1"), duration = 10, estadosPorDia = mapOf("LUN" to true)),
        TaskItem(actividad = "Enjabonarse", imageUrl = pictograma("paso_2"), duration = 10),
        TaskItem(actividad = "Secarse y vestirse", imageUrl = pictograma("paso_3"), duration = 15),
        TaskItem(actividad = "Peinarse", imageUrl = pictograma("paso_4"), duration = 5)
    )
    private val tareasTarde = listOf(TaskItem(actividad = "Hacer la tarea", duration = 20))

    private fun capturar(nombre: String, contenido: @Composable () -> Unit) {
        compose.setContent(contenido)
        compose.guardarCaptura(nombre)
    }

    // ── Modo niño ────────────────────────────────────────────────────────────

    private val perfil = SensoryProfile()

    @Test
    fun ninoInicio() = capturar("nino-inicio") {
        ChildTheme(perfil = perfil, turno = Turnos.MANANA) {
            TareasDelDia(tareasManana, tareasTarde, emptyList(), Turnos.MANANA, "LUNES") { _, _ -> }
        }
    }

    @Test
    fun ninoTarea() = capturar("nino-tarea") {
        ChildTheme(perfil = perfil, turno = Turnos.MANANA) {
            TareaEnCurso(tareasManana[1], perfil, 0.62f, "06:12", false, {}, {})
        }
    }

    @Test
    fun ninoEmociones() = capturar("nino-emociones") {
        ChildTheme(perfil = perfil.copy(voz = false), turno = Turnos.MANANA) {
            TaskFeedbackScreen(activityName = "Enjabonarse", onFeedbackSelected = {})
        }
    }

    @Test
    fun ninoLogro() = capturar("nino-logro") {
        ChildTheme(perfil = perfil.copy(voz = false), turno = Turnos.MANANA) {
            RoutineCompletedScreen(
                nextActivityPreview = "Secarse y vestirse",
                reinforcementMessages = listOf("¡Lo estás haciendo genial!"),
                onFinishClick = {}
            )
        }
    }

    @Test
    fun ninoTareaNoche() = capturar("nino-tarea-noche") {
        // Tema automático: la rutina de noche se ve en oscuro.
        ChildTheme(perfil = perfil, turno = Turnos.NOCHE) {
            TareaEnCurso(TaskItem(actividad = "Ponerse el pijama", duration = 10), perfil, 0.35f, "03:30", false, {}, {})
        }
    }

    @Test
    fun ninoTareaTierra() {
        val tierra = perfil.copy(paleta = AppPalette.TIERRA, temporizador = TimerStyle.BARRA)
        capturar("nino-tarea-tierra") {
            ChildTheme(perfil = tierra, turno = Turnos.MANANA) {
                TareaEnCurso(tareasManana[2], tierra, 0.8f, "12:00", false, {}, {})
            }
        }
    }

    // ── Panel del tutor ──────────────────────────────────────────────────────

    @Composable
    private fun Ajustes(palette: AppPalette, oscuro: Boolean) {
        UPadTheme(darkTheme = oscuro, palette = palette) {
            SettingsContent(
                isPremiumUser = true, isDarkMode = oscuro, palette = palette, appLanguage = "es", imageUri = null,
                onPaletteChange = {}, onDarkModeChange = {}, onLanguageChange = {}, onNavigateBack = {},
                onNavigateToSensoryProfile = {}, onNavigateToHelp = {}, onNavigateToProfile = {}
            )
        }
    }

    @Test
    fun tutorAjustes() = capturar("tutor-ajustes") { Ajustes(AppPalette.CALMA, oscuro = false) }

    @Test
    fun tutorAjustesTierraOscuro() = capturar("tutor-ajustes-tierra-oscuro") { Ajustes(AppPalette.TIERRA, oscuro = true) }

    @Test
    fun tutorPerfilSensorial() = capturar("tutor-perfil-sensorial") {
        UPadTheme(darkTheme = false, palette = AppPalette.CALMA) {
            SensoryProfileContent(perfil = perfil, onCambio = {}, onNavigateBack = {}, onNavigateToHelp = {}, onNavigateToProfile = {})
        }
    }
}
