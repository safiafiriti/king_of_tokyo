package com.example.kingoftokyo.game.model

data class Card(
    val id: Int,
    val name: String,
    val cost: Int,
    val type: CardType,
    val description: String,
    val imageResId: Int // Pour lier à l'image
)