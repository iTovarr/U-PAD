package com.example.upad.setup

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
import com.example.upad.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProfileSetupScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val pantallaPequeña = configuration.screenHeightDp < 650

    // 🔐 Instancias globales de Firebase
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val storage = remember { FirebaseStorage.getInstance() }

    // Estado para controlar la pantalla de carga durante la subida a la red
    var cargando by remember { mutableStateOf(false) }

    // --- PERSISTENCIA LOCAL (SharedPreferences) ---
    val prefs = remember { context.getSharedPreferences("UPAD_PREFS", Context.MODE_PRIVATE) }
    val esTemaOscuro by remember { mutableStateOf(prefs.getBoolean("TEMA_OSCURO", false)) }

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
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorAmarilloTEA = Color(0xFFFFD54F)

    val colorFondoBase = if (esTemaOscuro) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val colorSuperficie = if (esTemaOscuro) Color(0xFF1E293B) else Color.White
    val colorTextoPrincipal = if (esTemaOscuro) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val colorTextoSecundario = if (esTemaOscuro) Color(0xFF94A3B8) else Color(0xFF64748B)
    val colorBordeInput = if (esTemaOscuro) Color(0xFF334155) else Color(0xFFCBD5E1)
    val colorFondoIconoDefecto = if (esTemaOscuro) Color(0xFF334155) else Color(0xFFE2E8F0)
    val colorDivisor = if (esTemaOscuro) Color(0xFF334155) else Color(0xFFF1F5F9)

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
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = colorAzulTEA)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "CONFIGURACIÓN INICIAL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 12.dp),
                letterSpacing = 1.5.sp
            )
            Text(
                text = "¿Quién es el pequeño héroe?",
                fontSize = if (pantallaPequeña) 22.sp else 26.sp,
                fontWeight = FontWeight.Black,
                color = colorTextoPrincipal,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                lineHeight = if (pantallaPequeña) 28.sp else 32.sp
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
                    .clickable(enabled = !cargando) { galleryLauncher.launch("image/*") },
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
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.icon),
                            contentDescription = "Foto por defecto",
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
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar Foto",
                        tint = Color.White,
                        modifier = Modifier.padding(if (pantallaPequeña) 8.dp else 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (pantallaPequeña) 16.dp else 24.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorSuperficie),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(if (pantallaPequeña) 16.dp else 20.dp)) {

                    Text(
                        text = "INFORMACIÓN PERSONAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorAzulTEA,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        enabled = !cargando,
                        label = { Text("Nombre del niño", color = colorTextoSecundario) },
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
                        label = { Text("Edad", color = colorTextoSecundario) },
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
                        text = "GUSTOS Y PREFERENCIAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorAzulTEA,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = interests,
                        onValueChange = { interests = it },
                        enabled = !cargando,
                        label = { Text("¿Qué cosas le encantan?", color = colorTextoSecundario) },
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
                    val uidPadre = auth.currentUser?.uid

                    if (uidPadre != null) {
                        cargando = true

                        // Guardado preliminar local
                        prefs.edit().apply {
                            putString("CHILD_NAME", nombreFinal)
                            putString("CHILD_AGE", edadFinal)
                            putString("CHILD_INTERESTS", interesesFinal)
                            apply()
                        }

                        // Lógica de guardado en la nube
                        val guardarDatosEnFirestore = { urlDescarga: String ->
                            val perfilHijoMap = hashMapOf(
                                "nombre" to nombreFinal,
                                "edad" to edadFinal,
                                "intereses" to interesesFinal,
                                "fotoUrl" to urlDescarga,
                                "fechaRegistro" to com.google.firebase.Timestamp.now()
                            )

                            firestore.collection("usuarios").document(uidPadre)
                                .collection("hijos").document("perfil")
                                .set(perfilHijoMap, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener {
                                    cargando = false
                                    onSaveClick()
                                }
                                .addOnFailureListener { e ->
                                    cargando = false
                                    Toast.makeText(context, "Error al guardar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }

                        if (imageUri != null) {
                            // Subir archivo real a Firebase Storage estructurado en la ruta: /usuarios/{uid}/perfil_hijo.jpg
                            val storageRef = storage.reference.child("usuarios/$uidPadre/perfil_hijo.jpg")
                            storageRef.putFile(imageUri!!)
                                .addOnSuccessListener {
                                    // Conseguir el enlace HTTPS persistente
                                    storageRef.downloadUrl.addOnSuccessListener { uriDescarga ->
                                        guardarDatosEnFirestore(uriDescarga.toString())
                                    }
                                }
                                .addOnFailureListener { e ->
                                    cargando = false
                                    Toast.makeText(context, "Error al subir imagen: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            // Si el usuario no cargó foto propia, guardamos una cadena vacía o URL predeterminada
                            guardarDatosEnFirestore("")
                        }
                    } else {
                        Toast.makeText(context, "Sesión no válida. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (pantallaPequeña) 54.dp else 60.dp),
                enabled = !cargando,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "CONTINUAR CONFIGURACIÓN ➡️",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (esTemaOscuro) Color.Black else Color.White
                    )
                }
            }
        }
    }
}