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
        require(playerNames.isNotEmpty()) { "At least one player name required"}
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

    fun startTurn() {
        if (!currentPlayer().alive) {
            advanceToNextAlivePlayer()
        }

        val p = currentPlayer()

        if (p.location == Location.TOKYO) {
            p.victoryPoints += 2
        }

        dice.clear()
        rerollCount = 0
        pendingTokyoDecisionFor = null

        checkEndGame()
    }

    fun rollDice() {
        if (dice.isEmpty()) {
            dice = MutableList(6) { DiceSymbol.values().random() }
            rerollCount = 0
        }
    }

    fun rerollDice(indexes: List<Int>) {
        if (dice.isEmpty()) return
        if (rerollCount >= 2) return

        for (i in indexes) {
            if (i in 0 until dice.size) {
                dice[i] = DiceSymbol.values().random()
            }
        }
        rerollCount++
    }

    fun resolveDice() {
        if (dice.isEmpty()) return
        val current = currentPlayer()
        if (!current.alive) return

        val count = dice.groupingBy { it }.eachCount()

        val nbClaw = count[DiceSymbol.CLAW] ?: 0
        val nbHeart = count[DiceSymbol.HEART] ?: 0
        val nbEnergy = count[DiceSymbol.ENERGY] ?: 0
        val nbOne = count[DiceSymbol.ONE] ?: 0
        val nbTwo = count[DiceSymbol.TWO] ?: 0
        val nbThree = count[DiceSymbol.THREE] ?: 0

        applyNumberVp(current, DiceSymbol.ONE, nbOne)
        applyNumberVp(current, DiceSymbol.TWO, nbTwo)
        applyNumberVp(current, DiceSymbol.THREE, nbThree)

        if (current.location == Location.OUTSIDE && nbHeart > 0) {
            current.health = (current.health + nbHeart).coerceAtMost(10)
        }

        if (nbEnergy > 0){
            current.energy += nbEnergy
        }

        if (nbClaw > 0) {
            applyClawDamage(current, nbClaw)
        }

        eliminateDeadPlayers()

        if (!players.any { it.location == Location.TOKYO && it.alive } && current.alive) {
            current.location = Location.TOKYO
            current.victoryPoints += 1
        }

        checkEndGame()
    }

    fun leaveTokyo(attackedPlayer: Player) {
        if (attackedPlayer.location == Location.TOKYO && attackedPlayer.alive) {
            attackedPlayer.location = Location.OUTSIDE

            val attacker = currentPlayer()
            if (attacker.alive) {
                attacker.location = Location.TOKYO
                attacker.victoryPoints += 1
            }
        }

        pendingTokyoDecisionFor = null
        checkEndGame()
    }

    fun handleTokyoAfterAttack(attackedPlayer: Player) {}

    fun buyCard(cardId: Int) {

    }

    fun endTurn() {
        if (gameEnded) return
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (!currentPlayer().alive)

        dice.clear()
        rerollCount = 0
        pendingTokyoDecisionFor = null
    }

    fun checkEndGame() {
        val vpWinner = players.firstOrNull { it.alive && it.victoryPoints >= 20 }
        if (vpWinner != null) {
            gameEnded = true
            winnerId = vpWinner.id
            return
        }

        val alive = players.filter { it.alive }
        if (alive.size == 1) {
            gameEnded = true
            winnerId = alive.first().id
            return
        }

        if (alive.isEmpty()) {
            gameEnded = true
            winnerId = null
            return
        }
    }

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

    private fun advanceToNextAlivePlayer() {
        if (!::players.isInitialized || players.isEmpty()) return

        var nextIndex = currentPlayerIndex
        var loopCount = 0

        do {
            nextIndex = (nextIndex + 1) % players.size
            loopCount++
            if (loopCount > players.size) break // sécurité si tout le monde est mort
        } while (!players[nextIndex].alive)

        currentPlayerIndex = nextIndex
    }
}

