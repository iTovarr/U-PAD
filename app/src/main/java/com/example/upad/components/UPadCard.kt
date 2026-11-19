package com.example.upad.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.unit.dp

/** Radio de las esquinas de las tarjetas del panel del tutor. */
val UPadCardShape = RoundedCornerShape(20.dp)

/**
 * Título de una sección del panel del tutor. Se marca como encabezado para que
 * los lectores de pantalla permitan saltar de una sección a otra.
 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .padding(start = 4.dp)
            .semantics { heading() }
    )
}

/** Tarjeta base del panel del tutor: superficie de la paleta, sin sombra y con esquinas redondeadas. */
@Composable
fun UPadCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = UPadCardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
