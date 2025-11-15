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

    private fun applyNumberVp(player: Player, symbol: DiceSymbol, count: Int) {
        if (count >= 3) {
            val base = when(symbol) {
                DiceSymbol.ONE -> 1
                DiceSymbol.TWO -> 2
                DiceSymbol.THREE -> 3
                else -> 0
            }
            val extra = count - 3
            player.victoryPoints += base + extra
        }
    }

    private fun applyClawDamage(attacker: Player, damage: Int) {
        val attackerInTokyo = attacker.location == Location.TOKYO

        players.forEach { target ->
            if (!target.alive || target.id == attacker.id) return@forEach

            val shouldHit =
                if (attackerInTokyo) target.location == Location.OUTSIDE
                else target.location == Location.TOKYO

            if (shouldHit) {
                target.health -= damage
                if (target.health > 0 && target.location == Location.TOKYO) {
                    pendingTokyoDecisionFor = target
                }
            }
        }
    }

    private fun eliminateDeadPlayers() {
        players.forEach { p ->
            if (p.health <= 0) {
                p.alive = false
                p.location = Location.OUTSIDE
                p.energy = 0
                p.cards.clear()
            }
        }
    }

}