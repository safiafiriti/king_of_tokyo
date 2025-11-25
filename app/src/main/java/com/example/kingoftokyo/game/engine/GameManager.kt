package com.example.kingoftokyo.game.engine

import com.example.kingoftokyo.R
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

    var humanHitDamageThisTurn: Int? = null
        private set

    var gameEnded: Boolean = false
        private set

    var winnerId: Int? = null
        private set

    var roundNumber: Int = 1
        private set

    var pendingTokyoLeavePlayerId: Int? = null
        private set

    private var rollsThisTurn: Int = 0
    private val maxRollsPerTurn = 3

    // Pioche et Boutique
    private val deck: MutableList<Card> = mutableListOf()
    val marketCards: MutableList<Card?> = mutableListOf(null, null, null) // 3 choix

    fun initGame(playerNames: List<String>) {
        players.clear()
        roundNumber = 1
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

        // Initialiser la pioche et la boutique
        initDeck()
        refillMarket()
    }

    // Fonction qui initialise la pioche
    private fun initDeck() {
        deck.clear()
        // On crée les cartes manuellement en lien avec tes drawables
        deck.add(Card(1, "Rage Boost", 3, CardType.ACTION, "Gain energy on damage", R.drawable.rage_boost_card))
        deck.add(Card(2, "Armor Plating", 4, CardType.POWER, "Ignore 1 damage", R.drawable.iron_shell_card))
        deck.add(Card(3, "Energy Magnet", 3, CardType.POWER, "Gain energy when others do", R.drawable.energy_magnet_card))
        deck.add(Card(4, "Spiky Armor", 5, CardType.POWER, "Deal dmg when attacked", R.drawable.spiky_armor_card))
        deck.add(Card(5, "Solar Absorbal", 4, CardType.POWER, "Gain energy on other's loss", R.drawable.solar_absorbal_card))
        deck.add(Card(6, "Mega Battery", 4, CardType.POWER, "Gain energy at start turn", R.drawable.mega_battery_card))
        deck.add(Card(7, "Toxic Breath", 5, CardType.POWER, "Extra damage on attack", R.drawable.toxic_vreath_card))
        deck.add(Card(8, "Colossal Claws", 7, CardType.POWER, "Extra damage", R.drawable.colossal_claws))
        deck.add(Card(9, "Regeneration", 6, CardType.POWER, "Heal at start turn", R.drawable.regeneration_core_card))
        deck.add(Card(10, "Super Reroll", 4, CardType.POWER, "+1 Reroll", R.drawable.super_reroll_card))

        deck.shuffle() // Mélanger pour l'aléatoire
    }

    private fun refillMarket() {
        for (i in 0 until 3) {
            if (marketCards[i] == null && deck.isNotEmpty()) {
                marketCards[i] = deck.removeAt(0)
            }
        }
    }

    // Retourne true si l'achat a réussi
    fun buyCard(player: Player, marketIndex: Int): Boolean {
        val card = marketCards.getOrNull(marketIndex) ?: return false

        if (player.energy >= card.cost) {
            player.spendEnergy(card.cost)

            // Appliquer l'effet (Simplifié ici : on ajoute juste à la liste du joueur)
            if (card.type == CardType.POWER) {
                player.cards.add(card)
            } else {
                // Action immédiate (à implémenter selon l'effet)
                // Ex: if(card.name == "Heal") player.heal(2)
            }

            // Retirer de la boutique et remplacer
            marketCards[marketIndex] = null
            refillMarket()
            return true
        }
        return false
    }

    fun startTurn() {
        if (gameEnded) return

        rollsThisTurn = 0
//        dice.forEach { die ->
//            die.unlock()
//        }
    }

    val currentPlayer: Player
        get() = players[currentPlayerIndex]


    fun rollDice() {
        if (gameEnded) return
        if (rollsThisTurn >= maxRollsPerTurn) return

        if (rollsThisTurn == 0) {
            dice.forEach { it.unlock() }
        }

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

    fun lockAllDice() {
        dice.forEach { it.lock() }
    }

    fun resolveDice() {
        if (gameEnded) return
        val player = currentPlayer
        if (!player.alive) return

        humanHitDamageThisTurn = null

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
            players.filter { it.alive && it.id != attacker.id }
                .forEach { target ->
                    target.takeDamage(damage)
                    // ici il n'y a personne dans Tokyo donc pas de popup
                }
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

                if (target.isHuman) {
                    humanHitDamageThisTurn = (humanHitDamageThisTurn ?: 0) + damage
                }

                // Si un joueur DANS Tokyo se fait taper
                if (target.location == Location.TOKYO && target.alive) {

                    // Si c’est un humain : demander popup
                    if (target.isHuman) {
                        pendingTokyoLeavePlayerId = target.id
                    }
                }
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

        if (nextIndex == 0) {
            roundNumber++
        }

        currentPlayerIndex = nextIndex
        rollsThisTurn = 0
    }

    private fun checkEndOfGame() {
        val alivePlayers = players.filter { it.alive }
        val winnerByVP = players.firstOrNull { it.victoryPoints >= 20 }
        val human = players.firstOrNull { it.isHuman }

        when {
            winnerByVP != null -> {
                gameEnded = true
                winnerId = winnerByVP.id
            }
            human != null && !human.alive -> {
                gameEnded = true
                // on peut mettre comme "winner" n'importe quel bot encore vivant
                winnerId = alivePlayers.firstOrNull { it.id != human.id }?.id
            }
            alivePlayers.size == 1 -> {
                gameEnded = true
                winnerId = alivePlayers.first().id
            }
        }
    }

    fun resolveTokyoLeaveChoice(leave: Boolean) {
        val playerId = pendingTokyoLeavePlayerId ?: return
        val tokyoPlayer = players.firstOrNull { it.id == playerId && it.location == Location.TOKYO }

        if (leave && tokyoPlayer != null) {
            tokyoPlayer.leaveTokyo()
            currentPlayer.enterTokyo()
        }

        pendingTokyoLeavePlayerId = null
    }

    fun clearHumanHitDamage() {
        humanHitDamageThisTurn = null
    }

}
