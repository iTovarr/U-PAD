package com.example.upad.routines

import com.example.upad.ui.theme.extendedColors
import androidx.compose.ui.semantics.Role
import com.example.upad.R
import androidx.compose.ui.res.stringResource
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

    val colorAzulTEA = MaterialTheme.colorScheme.primary
    val colorFondoBase = MaterialTheme.colorScheme.background

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
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.cd_back), tint = colorAzulTEA)
            }
            Text(
                text = stringResource(id = R.string.pictogram_overline),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = stringResource(id = R.string.pictogram_title),
                color = colorAzulTEA,
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.headlineSmall
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
                placeholder = { Text(stringResource(id = R.string.pictogram_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorAzulTEA) },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
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

            // Atribución exigida por la licencia de los pictogramas (CC BY-NC-SA)
            Text(
                text = stringResource(id = R.string.arasaac_attribution),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                style = MaterialTheme.typography.labelSmall
            )

            // BOTÓN DE CARGA (OPCIONAL)
            Button(
                onClick = { /* Lógica propia */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = colorAzulTEA)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(id = R.string.pictogram_add_photo), color = colorAzulTEA, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun PictogramCard(title: String, imageUrl: String, onClick: () -> Unit) {
    val colorAzulTEA = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable(role = Role.Button) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    .background(MaterialTheme.extendedColors.fondoPictograma),
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
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}