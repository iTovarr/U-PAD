package com.example.upad.data

import com.example.upad.R

data class RutinaPaso(
    val id: Int,
    val titulo: String,
    val imagenRes: Int
)

val rutinaBanoLista = listOf(
    RutinaPaso(1, "ME QUITO LA ROPA Y ENTRO EN LA DUCHA", R.drawable.paso_1),
    RutinaPaso(2, "ME ENJABONO LA CABEZA Y EL CUERPO", R.drawable.paso_2),
    RutinaPaso(3, "ME ACLARO, SALGO, ME SECO Y VISTO", R.drawable.paso_3),
    RutinaPaso(4, "ME SECO EL PELO, ME ECHO COLONIA Y LISTO!", R.drawable.paso_4)
)