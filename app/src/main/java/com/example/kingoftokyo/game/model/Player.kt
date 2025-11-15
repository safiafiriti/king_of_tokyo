package com.example.kingoftokyo.game.model

data class Player(
    val id: Int,
    val name: String,
    val isHuman: Boolean,
    var health: Int = 0,
    var victoryPoints: Int = 0,
    var energy: Int = 0,
    var location: Location = Location.OUTSIDE,
    var alive: Boolean = true,
    val cards: MutableList<Card> = mutableListOf()
)
