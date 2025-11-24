package com.example.kingoftokyo.game.engine

import com.example.kingoftokyo.game.model.Card
import com.example.kingoftokyo.game.model.CardType
import com.example.kingoftokyo.game.model.DiceSymbol
import com.example.kingoftokyo.game.model.GameDie
import com.example.kingoftokyo.game.model.Location
import com.example.kingoftokyo.game.model.Player

class GameManager {

    val players: MutableList<Player> = mutableListOf()
    val dice: MutableList<GameDie> =
        MutableList(6) { GameDie(DiceSymbol.values().random(), locked = false) }

    var currentPlayerIndex: Int = 0
        private set

    var gameEnded: Boolean = false
        private set

    var winnerId: Int? = null
        private set

    private var rollsThisTurn: Int = 0
    private val maxRollsPerTurn = 3


    fun initGame(playerNames: List<String>) {
        players.clear()
        playerNames.forEachIndexed { index, name ->
            players += Player(
                id = index,
                name = name,
                isHuman = (index == 0),
                health = 10,
                energy = 0,
                victoryPoints = 0,
                location = Location.OUTSIDE,
                alive = true,
                cards = mutableListOf()
            )
        }

        currentPlayerIndex = 0
        gameEnded = false
        winnerId = null
        rollsThisTurn = 0

        dice.forEach {
            it.unlock()
            it.roll()
        }
    }

    fun startTurn() {
        if (gameEnded) return

        rollsThisTurn = 0
        dice.forEach { die ->
            die.unlock()
        }
    }

    val currentPlayer: Player
        get() = players[currentPlayerIndex]


    fun rollDice() {
        if (gameEnded) return
        if (rollsThisTurn >= maxRollsPerTurn) return

        dice.forEach { it.roll() }
        rollsThisTurn++
    }

    fun rerollDice(indices: List<Int>) {
        if (gameEnded) return
        if (rollsThisTurn >= maxRollsPerTurn) return

        indices.forEach { i ->
            if (i in dice.indices) {
                dice[i].roll()
            }
        }
        rollsThisTurn++
    }

    fun toggleLock(index: Int) {
        if (index !in dice.indices) return
        if (rollsThisTurn == 0) return

        dice[index].toggleLock()
    }


    fun resolveDice() {
        if (gameEnded) return
        val player = currentPlayer
        if (!player.alive) return

        var claws = 0
        var hearts = 0
        var energy = 0

        val ones = dice.count { it.symbol == DiceSymbol.ONE }
        val twos = dice.count { it.symbol == DiceSymbol.TWO }
        val threes = dice.count { it.symbol == DiceSymbol.THREE }

        dice.forEach { die ->
            when (die.symbol) {
                DiceSymbol.CLAW   -> claws++
                DiceSymbol.HEART  -> hearts++
                DiceSymbol.ENERGY -> energy++
                else -> { /* ONE/TWO/THREE déjà comptés */ }
            }
        }

        if (claws > 0) {
            applyClawDamage(player, claws)
        }

        if (hearts > 0 && player.location == Location.OUTSIDE) {
            player.heal(hearts)
        }

        if (energy > 0) {
            player.gainEnergy(energy)
        }

        var gainedVP = 0
        gainedVP += victoryFromNumbers(1, ones)
        gainedVP += victoryFromNumbers(2, twos)
        gainedVP += victoryFromNumbers(3, threes)
        if (gainedVP > 0) {
            player.gainVictory(gainedVP)
        }

        checkEndOfGame()
    }

    private fun applyClawDamage(attacker: Player, damage: Int) {
        val attackerInTokyo = attacker.location == Location.TOKYO
        val tokyoOccupied = players.any { it.location == Location.TOKYO && it.alive }

        if (!tokyoOccupied) {
            attacker.enterTokyo()
            return
        }

        players.filter { it.alive && it.id != attacker.id }.forEach { target ->
            val targetInTokyo = target.location == Location.TOKYO

            val shouldHit =
                (attackerInTokyo && !targetInTokyo) ||
                        (!attackerInTokyo && targetInTokyo)

            if (shouldHit) {
                target.takeDamage(damage)
            }
        }
    }

    private fun victoryFromNumbers(number: Int, count: Int): Int {
        if (count < 3) return 0
        val extra = count - 3
        // 3x1 → 1 point ; 3x2 → 2 points ; 3x3 → 3 points + extras
        return number + extra
    }

    fun endTurn() {
        if (gameEnded) return

        var nextIndex = currentPlayerIndex
        var safety = 0

        do {
            nextIndex = (nextIndex + 1) % players.size
            safety++
        } while (!players[nextIndex].alive && safety <= players.size)

        currentPlayerIndex = nextIndex
        rollsThisTurn = 0
    }

    private fun checkEndOfGame() {
        val alivePlayers = players.filter { it.alive }
        val winnerByVP = players.firstOrNull { it.victoryPoints >= 20 }

        when {
            winnerByVP != null -> {
                gameEnded = true
                winnerId = winnerByVP.id
            }
            alivePlayers.size == 1 -> {
                gameEnded = true
                winnerId = alivePlayers.first().id
            }
        }
    }
}
