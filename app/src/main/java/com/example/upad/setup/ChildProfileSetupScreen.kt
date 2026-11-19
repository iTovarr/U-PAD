package com.example.upad.setup

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.upad.di.LocalAppContainer
import com.example.upad.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProfileSetupScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val pantallaPequeña = configuration.screenHeightDp < 650

    // 🔐 Repositorios de sesión y de datos del tutor
    val container = LocalAppContainer.current
    val session = container.sessionRepository
    val parentProfileRepository = container.parentProfileRepository
    val scope = rememberCoroutineScope()
    // Plantillas de mensajes: se leen aquí porque los Toast se muestran fuera de la composición
    val msgErrorImagen = stringResource(id = R.string.error_upload_image)
    val msgErrorPerfil = stringResource(id = R.string.error_save_profile)
    val msgSesionInvalida = stringResource(id = R.string.error_invalid_session)

    // Estado para controlar la pantalla de carga durante la subida a la red
    var cargando by remember { mutableStateOf(false) }

    // --- PERSISTENCIA LOCAL (SharedPreferences) ---
    val prefs = remember { context.getSharedPreferences("UPAD_PREFS", Context.MODE_PRIVATE) }

    var name by remember { mutableStateOf(prefs.getString("CHILD_NAME", "") ?: "") }
    var age by remember { mutableStateOf(prefs.getString("CHILD_AGE", "") ?: "") }
    var interests by remember { mutableStateOf(prefs.getString("CHILD_INTERESTS", "") ?: "") }

    var imageUri by remember {
        mutableStateOf<Uri?>(prefs.getString("CHILD_PHOTO_URI", null)?.let { Uri.parse(it) })
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            prefs.edit().putString("CHILD_PHOTO_URI", uri.toString()).apply()
        }
    }

    // --- CONFIGURACIÓN DE COLORES MULTI-TEMA ---
val colorAzulTEA = MaterialTheme.colorScheme.primary
val colorAmarilloTEA = MaterialTheme.colorScheme.tertiaryContainer

val colorFondoBase = MaterialTheme.colorScheme.background
val colorSuperficie = MaterialTheme.colorScheme.surfaceContainerLowest
val colorTextoPrincipal = MaterialTheme.colorScheme.onSurface
val colorTextoSecundario = MaterialTheme.colorScheme.onSurfaceVariant
val colorBordeInput = MaterialTheme.colorScheme.outline
val colorFondoIconoDefecto = MaterialTheme.colorScheme.surfaceVariant
val colorDivisor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(colorSuperficie)
                .padding(
                    top = if (pantallaPequeña) 30.dp else 40.dp,
                    bottom = if (pantallaPequeña) 16.dp else 24.dp,
                    start = 16.dp,
                    end = 24.dp
                )
        ) {
            IconButton(onClick = onBackClick, enabled = !cargando) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.cd_back), tint = colorAzulTEA)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(id = R.string.child_setup_overline),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = stringResource(id = R.string.child_setup_title),
                color = colorTextoPrincipal,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                lineHeight = if (pantallaPequeña) 28.sp else 32.sp,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // --- FORMULARIO ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(if (pantallaPequeña) 16.dp else 24.dp))

            // Avatar interactivo
            Box(
                modifier = Modifier
                    .size(if (pantallaPequeña) 120.dp else 140.dp)
                    .clickable(enabled = !cargando, role = Role.Button) { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(4.dp, colorAmarilloTEA, CircleShape)
                        .clip(CircleShape),
                    color = colorFondoIconoDefecto
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = stringResource(id = R.string.cd_profile_photo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.icon),
                            contentDescription = stringResource(id = R.string.cd_default_photo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(if (pantallaPequeña) 36.dp else 40.dp)
                        .clip(CircleShape)
                        .border(2.dp, colorSuperficie, CircleShape),
                    color = colorAzulTEA,
                    shadowElevation = 0.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(id = R.string.child_change_photo),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(if (pantallaPequeña) 8.dp else 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (pantallaPequeña) 16.dp else 24.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorSuperficie),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(if (pantallaPequeña) 16.dp else 20.dp)) {

                    Text(
                        text = stringResource(id = R.string.child_personal_info),
                        color = colorAzulTEA,
                        modifier = Modifier.padding(bottom = 12.dp),
                        style = MaterialTheme.typography.labelSmall
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        enabled = !cargando,
                        label = { Text(stringResource(id = R.string.child_name_label), color = colorTextoSecundario) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colorTextoSecundario) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorAzulTEA,
                            unfocusedBorderColor = colorBordeInput,
                            focusedContainerColor = colorSuperficie,
                            unfocusedContainerColor = colorSuperficie,
                            focusedTextColor = colorTextoPrincipal,
                            unfocusedTextColor = colorTextoPrincipal
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        enabled = !cargando,
                        label = { Text(stringResource(id = R.string.child_age_label), color = colorTextoSecundario) },
                        leadingIcon = { Icon(Icons.Default.Face, contentDescription = null, tint = colorTextoSecundario) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorAzulTEA,
                            unfocusedBorderColor = colorBordeInput,
                            focusedContainerColor = colorSuperficie,
                            unfocusedContainerColor = colorSuperficie,
                            focusedTextColor = colorTextoPrincipal,
                            unfocusedTextColor = colorTextoPrincipal
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colorDivisor)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(id = R.string.child_preferences),
                        color = colorAzulTEA,
                        modifier = Modifier.padding(bottom = 12.dp),
                        style = MaterialTheme.typography.labelSmall
                    )

                    OutlinedTextField(
                        value = interests,
                        onValueChange = { interests = it },
                        enabled = !cargando,
                        label = { Text(stringResource(id = R.string.child_interests_label), color = colorTextoSecundario) },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = colorTextoSecundario) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorAzulTEA,
                            unfocusedBorderColor = colorBordeInput,
                            focusedContainerColor = colorSuperficie,
                            unfocusedContainerColor = colorSuperficie,
                            focusedTextColor = colorTextoPrincipal,
                            unfocusedTextColor = colorTextoPrincipal
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- BOTÓN INFERIOR ADAPTADO ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorSuperficie)
                .padding(horizontal = 20.dp, vertical = if (pantallaPequeña) 12.dp else 16.dp)
        ) {
            Button(
                onClick = {
                    val nombreFinal = name.ifBlank { "Mateo" }
                    val edadFinal = age.ifBlank { "6" }
                    val interesesFinal = interests.ifBlank { "Dinosaurios, Rompecabezas" }
                    val uidPadre = session.currentUser?.uid

                    if (uidPadre != null) {
                        cargando = true

                        // Guardado preliminar local
                        prefs.edit().apply {
                            putString("CHILD_NAME", nombreFinal)
                            putString("CHILD_AGE", edadFinal)
                            putString("CHILD_INTERESTS", interesesFinal)
                            apply()
                        }

                        // Guardado en la nube: primero la foto (si hay) y después el perfil
                        val fotoLocal = imageUri
                        scope.launch {
                            val fotoUrl = if (fotoLocal != null) {
                                try {
                                    parentProfileRepository.uploadChildPhoto(uidPadre, fotoLocal)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    cargando = false
                                    Toast.makeText(context, msgErrorImagen.format(e.message.orEmpty()), Toast.LENGTH_LONG).show()
                                    return@launch
                                }
                            } else {
                                // Sin foto propia se guarda una cadena vacía
                                ""
                            }

                            try {
                                parentProfileRepository.saveChildProfile(
                                    uid = uidPadre,
                                    nombre = nombreFinal,
                                    edad = edadFinal,
                                    intereses = interesesFinal,
                                    fotoUrl = fotoUrl
                                )
                                cargando = false
                                onSaveClick()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                cargando = false
                                Toast.makeText(context, msgErrorPerfil.format(e.message.orEmpty()), Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, msgSesionInvalida, Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (pantallaPequeña) 54.dp else 60.dp),
                enabled = !cargando,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = stringResource(id = R.string.child_continue),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}