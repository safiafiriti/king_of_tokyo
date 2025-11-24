package com.example.kingoftokyo.game.model

data class Player(
    val id: Int,
    val name: String,
    val isHuman: Boolean,
    var health: Int = 10,
    var energy: Int = 0,
    var victoryPoints: Int = 0,
    var location: Location = Location.OUTSIDE,
    var alive: Boolean = true,
    val cards: MutableList<Card> = mutableListOf()
) {

    fun takeDamage(amount: Int) {
        if (!alive) return

        health -= amount
        if (health <= 0) {
            health = 0
            alive = false
            location = Location.OUTSIDE
        }
    }

    fun heal(amount: Int) {
        if (location == Location.TOKYO) return
        health = minOf(10, health + amount)
    }

    fun gainEnergy(amount: Int) {
        energy += amount
    }

    fun spendEnergy(amount: Int): Boolean {
        if (energy >= amount) {
            energy -= amount
            return true
        }
        return false
    }

    fun gainVictory(points: Int) {
        victoryPoints += points
    }

    fun enterTokyo() {
        location = Location.TOKYO
        gainVictory(1)
    }

    fun leaveTokyo() {
        location = Location.OUTSIDE
    }
}
