package com.example.kingoftokyo.game.engine

import com.example.kingoftokyo.game.model.*
import kotlin.random.Random

class GameManager {

    lateinit var players: MutableList<Player>
    var currentPlayerIndex: Int = 0

    var dice: MutableList<DiceSymbol> = mutableListOf()
    var rerollCount: Int = 0

    var gameEnded: Boolean = false
    var winnerId: Int? = null

    var pendingTokyoDecisionFor: Player? = null

    fun initGame(playerNames: List<String>) {
        val list = mutableListOf<Player>()

        list.add(
            Player(
                id = 0,
                name = playerNames[0],
                isHuman = true
            )
        )

        for (i in 1..3) {
            val name = if (i < playerNames.size) playerNames[i] else "Bot $i"
            list.add(
                Player(
                    id = i,
                    name = name,
                    isHuman = false
                )
            )
        }

        players = list
        currentPlayerIndex = 0
        dice.clear()
        rerollCount = 0
        gameEnded = false
        winnerId = null
        pendingTokyoDecisionFor = null
    }

    fun startTurn() {}

    fun rollDice(){}

    fun rerollDice(indexes: List<Int>) {}

    fun resolveDice() {}

    fun handleTokyoAfterAttack(attackedPlayer: Player) {}

    fun buyCard(cardId: Int) {}

    fun endTurn() {}

    fun checkEndGame() {}

    fun currentPlayer(): Player {
        return players[currentPlayerIndex]
    }

}