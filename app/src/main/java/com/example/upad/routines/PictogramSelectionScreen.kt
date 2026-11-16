package com.example.upad.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // <--- Asegúrate de tener esta importación
import com.example.upad.viewmodel.RoutineViewModel

@Composable
fun PictogramSelectionScreen(
    viewModel: RoutineViewModel, // <--- Pasamos el ViewModel
    onBackClick: () -> Unit,
    onPictogramSelected: (String, String) -> Unit // Ahora devuelve Nombre y URL
) {
    var searchQuery by remember { mutableStateOf("") }
    // Escuchamos los resultados de la API que llegan al ViewModel
    val apiResults by viewModel.searchResults.collectAsState()

    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)

    // Lanzar la búsqueda cada vez que cambia el texto (mínimo 3 letras)
    LaunchedEffect(searchQuery) {
        viewModel.searchArasaac(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = colorAzulTEA)
            }
            Text(
                text = "BIBLIOTECA ARASAAC",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 12.dp)
            )
            Text(
                text = "Busca un Pictograma",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // BUSCADOR REAL
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Lavarse las manos...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorAzulTEA) },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = colorAzulTEA,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // GRILLA DE RESULTADOS REALES
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(apiResults) { pictogram ->
                    val name = pictogram.keywords.firstOrNull()?.keyword?.uppercase() ?: "SIN NOMBRE"
                    val imageUrl = "https://api.arasaac.org/api/pictograms/${pictogram._id}"

                    PictogramCard(
                        title = name,
                        imageUrl = imageUrl,
                        onClick = { onPictogramSelected(name, imageUrl) }
                    )
                }
            }

            // BOTÓN DE CARGA (OPCIONAL)
            Button(
                onClick = { /* Lógica propia */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(2.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = colorAzulTEA)
                Spacer(modifier = Modifier.width(12.dp))
                Text("AÑADIR FOTO PROPIA", color = colorAzulTEA, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun PictogramCard(title: String, imageUrl: String, onClick: () -> Unit) {
    val colorAzulTEA = Color(0xFF4FC3F7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp).fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF8F9FA)),
                contentAlignment = Alignment.Center
            ) {
                // AQUÍ SE CARGA LA IMAGEN DE ARASAAC
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}