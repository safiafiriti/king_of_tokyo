package com.example.kingoftokyo.game.engine

import com.example.kingoftokyo.game.model.*
import kotlin.random.Random

class GameManager {
    lateinit var players: MutableList<Player>
    var currentPlayer: Int = 0

    var dice: MutableList<DiceSymbol> = mutableListOf()
    var rerollCount: Int = 0

    var gameEnded: Boolean = false
    var winnerId: Int? = null

    var pendingTokyoDecisionFor: Player? = null

    fun initGame(playerName: List<String>) {
    }

    fun startTurn() {}

    fun rollDice(){}

    fun rerollDice(indexes: List<Int>) {}

    fun resolveDice() {}

    fun handleTokyoAfterAttack(attackedPlayer: Player) {}

    fun buyCard(cardId: Int) {}

    fun endTurn() {}

    fun checkEndGame() {}

}