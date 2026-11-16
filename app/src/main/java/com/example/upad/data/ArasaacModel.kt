package com.example.upad.data

data class ArasaacPictogram(
    val _id: Int,
    val keywords: List<Keyword>
)

data class Keyword(
    val keyword: String,
    val type: Int
)