package com.example.kingoftokyo.game.model

data class GameDie(
    var symbol: DiceSymbol,
    var locked: Boolean = false
) {

    fun roll() {
        if (!locked) {
            symbol = DiceSymbol.values().random()
        }
    }

    fun lock() {
        locked = true
    }

    fun unlock() {
        locked = false
    }

    fun toggleLock() {
        locked = !locked
    }
}