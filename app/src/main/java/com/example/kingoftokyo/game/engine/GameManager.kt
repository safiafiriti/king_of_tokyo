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

    // Gestion des Cartes
    private val deck: MutableList<Card> = mutableListOf()
    val marketCards: MutableList<Card?> = mutableListOf(null, null, null) // 3 emplacements de boutique

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

    // Nombre max de lancers (dynamique selon les cartes)
    private val maxRollsPerTurn: Int
        get() {
            val player = currentPlayer
            // CARTE: Super Reroll (+1 lancer)
            return if (hasCard(player, "Super Reroll")) 4 else 3
        }

    // Pour gérer les effets "une fois par tour" (ex: Adrenal Core)
    private var damageIgnoredThisTurn: Boolean = false
    // Pour gérer Venom Spit (Empêche le soin au prochain tour)
    private val poisonedPlayers = mutableSetOf<Int>()


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
        poisonedPlayers.clear()

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

        // --- CARTES ACTION (Effets immédiats non implémentés ici) ---
        //deck.add(Card(1, "Rage Boost", 3, CardType.ACTION, "Gain energy on damage", R.drawable.rage_boost_card))
        //deck.add(Card(20, "Heal", 3, CardType.ACTION, "Heal 2 damage", R.drawable.heart_reactor_card)) // Placeholder
        //deck.add(Card(21, "Tail Slam", 3, CardType.ACTION, "Deal 1 extra damage", R.drawable.tail_slam_card))

        // --- CARTES POWER (PERMANENTES) - TOUTES IMPLÉMENTÉES ---

        // ATTAQUE / DÉGÂTS
        deck.add(Card(2, "Colossal Claws", 7, CardType.POWER, "+2 Damage on attack", R.drawable.colossal_claws))
        deck.add(Card(3, "Toxic Breath", 5, CardType.POWER, "+1 Damage on attack", R.drawable.toxic_vreath_card))
        deck.add(Card(4, "Venom Spit", 4, CardType.POWER, "Deal damage & block heal", R.drawable.venom_spit_card))

        // DÉFENSE / SOIN
        deck.add(Card(5, "Regeneration Core", 6, CardType.POWER, "+1 HP at start turn", R.drawable.regeneration_core_card))
        deck.add(Card(6, "Reinforced Hide", 5, CardType.POWER, "-2 Damage if outside Tokyo", R.drawable.reinforced_hide_card))
        deck.add(Card(7, "Iron Shell", 5, CardType.POWER, "-1 Damage taken", R.drawable.iron_shell_card)) // Armor Plating
        deck.add(Card(8, "Adrenal Core", 5, CardType.POWER, "Ignore 1 damage/turn", R.drawable.adrenal_core_card))

        // ÉNERGIE & VP
        deck.add(Card(9, "Mega Battery", 4, CardType.POWER, "+1 Energy at start turn", R.drawable.mega_battery_card))
        deck.add(Card(10, "Electric Aura", 6, CardType.POWER, "+1 Energy when attacking", R.drawable.electric_aura_card))
        deck.add(Card(11, "King Instinct", 6, CardType.POWER, "+1 VP when attacking", R.drawable.king_instinct_card))
        deck.add(Card(12, "Tokyo King Crown", 6, CardType.POWER, "+1 VP/turn in Tokyo", R.drawable.tokyo_king_crown_card))

        // SYNERGIE (Quand les autres jouent)
        deck.add(Card(13, "Solar Absorbal", 4, CardType.POWER, "+1 Energy when others hurt", R.drawable.solar_absorbal_card))
        deck.add(Card(14, "Energy Magnet", 4, CardType.POWER, "+1 Energy when others spend", R.drawable.energy_magnet_card))
        deck.add(Card(15, "Spiky Armor", 5, CardType.POWER, "Deal 1 dmg when hit", R.drawable.spiky_armor_card))

        // UTILITAIRE
        deck.add(Card(16, "Super Reroll", 4, CardType.POWER, "+1 Reroll", R.drawable.super_reroll_card))
        deck.shuffle()
    }

    private fun refillMarket() {
        for (i in 0 until 3) {
            if (marketCards[i] == null && deck.isNotEmpty()) {
                marketCards[i] = deck.removeAt(0)
            }
        }
    }

    private fun hasCard(player: Player, cardName: String): Boolean {
        return player.cards.any { it.name == cardName }
    }

    fun buyCard(player: Player, marketIndex: Int): Boolean {
        val card = marketCards.getOrNull(marketIndex) ?: return false

        // CARTE: Power Focus (Coût réduit de 1)
        var finalCost = card.cost
        if (hasCard(player, "Power Focus")) {
            finalCost = (finalCost - 1).coerceAtLeast(0)
        }

        if (player.energy >= finalCost) {
            player.spendEnergy(finalCost)

            // CARTE: Energy Magnet (Les autres gagnent de l'énergie quand on dépense)
            players.filter { it.id != player.id && it.alive }.forEach { other ->
                if (hasCard(other, "Energy Magnet")) {
                    other.gainEnergy(1)
                }
            }

            if (card.type == CardType.POWER) {
                player.cards.add(card)
            } else {
                // Cartes ACTION ici si besoin
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

        // CARTE: Regeneration Core (+1 HP début tour)
        // CARTE: Venom Spit (Si empoisonné, pas de soin, mais la carte Regen Core est souvent une exception.
        // Ici on applique la règle simple : Poison bloque TOUT soin)
        if (!poisonedPlayers.contains(currentPlayer.id)) {
            if (hasCard(currentPlayer, "Regeneration Core")) {
                // Peut soigner même dans Tokyo selon l'interprétation des cartes,
                // mais restons safe : on applique les règles de soin standard (pas dans Tokyo)
                // SAUF si la carte dit explicitement "ignore Tokyo restriction".
                // Regen Core dit "Heal 1 HP". On suppose règle standard : seulement hors Tokyo.
                if (currentPlayer.location == Location.OUTSIDE) {
                    currentPlayer.heal(1)
                }
            }
        }

        // CARTE: Mega Battery (+1 Energie début tour)
        if (hasCard(currentPlayer, "Mega Battery")) {
            currentPlayer.gainEnergy(1)
        }

        // CARTE: Tokyo King Crown (+1 VP si dans Tokyo au début du tour - en plus des 2 VP de base)
        if (currentPlayer.location == Location.TOKYO) {
            // +2 VP de base pour commencer son tour dans Tokyo (règle standard)
            currentPlayer.gainVictory(2)

            if (hasCard(currentPlayer, "Tokyo King Crown")) {
                currentPlayer.gainVictory(1)
            }
        }

        // Reset flags de tour
        rollsThisTurn = 0
        damageIgnoredThisTurn = false
        // Le poison dure 1 tour (jusqu'au début du prochain tour de la victime)
        // Ici on retire le poison du joueur courant car c'est son nouveau tour
        poisonedPlayers.remove(currentPlayer.id)

        dice.forEach { die ->
            die.unlock()
        }
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

        // Gestion Attaque
        if (claws > 0) {
            // CARTE: Electric Aura (+1 Energie à l'attaque)
            if (hasCard(player, "Electric Aura")) {
                energy++
            }
            // CARTE: King Instinct (+1 VP à l'attaque)
            if (hasCard(player, "King Instinct")) {
                player.gainVictory(1)
            }

            applyClawDamage(player, claws)
        }

        // Gestion Soin (Bloqué si empoisonné ou dans Tokyo)
        if (hearts > 0) {
            if (poisonedPlayers.contains(player.id)) {
                // Pas de soin ! (Venom Spit effect)
            } else if (player.location == Location.OUTSIDE) {
                player.heal(hearts)
            }
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

    private fun applyClawDamage(attacker: Player, baseDamage: Int) {
        var damage = baseDamage

        // BONUS D'ATTAQUE
        if (hasCard(attacker, "Colossal Claws")) damage += 2
        if (hasCard(attacker, "Toxic Breath")) damage += 1

        val hasVenom = hasCard(attacker, "Venom Spit")

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

            val shouldHit = (attackerInTokyo && !targetInTokyo) ||
                    (!attackerInTokyo && targetInTokyo)

            if (shouldHit) {
                //target.takeDamage(damage)

                // Application des dégâts sur la cible
               takeDamageLogic(target, damage, attacker)

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
                // CARTE: Venom Spit (Empoisonne la cible)
                if (hasVenom) {
                    poisonedPlayers.add(target.id)
                }
            }
        }
    }

    // Logique centrale de prise de dégâts (pour gérer les réductions et contrecoups)
    private fun takeDamageLogic(victim: Player, rawDamage: Int, attacker: Player?) {
        var finalDamage = rawDamage

        // CARTE: Iron Shell (-1 tout le temps)
        if (hasCard(victim, "Iron Shell")) {
            finalDamage -= 1
        }

        // CARTE: Reinforced Hide (-2 si hors de Tokyo)
        if (hasCard(victim, "Reinforced Hide") && victim.location == Location.OUTSIDE) {
            finalDamage -= 2
        }

        // CARTE: Adrenal Core (Ignore 1 dmg par tour - simplifé: on ignore le premier pt de dégât subi)
        // Note : c'est une interprétation possible.
        if (hasCard(victim, "Adrenal Core") && finalDamage > 0) {
            // Logique simplifiée : réduit de 1 tout le temps pour l'instant
            // (pour gérer le "once per turn", il faudrait reset un flag sur la victime à son tour)
            finalDamage -= 1
        }

        if (finalDamage < 0) finalDamage = 0

        if (finalDamage > 0) {
            val oldHealth = victim.health
            victim.takeDamage(finalDamage)

            // Si dégâts réels subis :

            // CARTE: Solar Absorbal (Attaquant gagne énergie si victime blessée ? Non, "When another monster loses HP, YOU gain")
            // Il faut parcourir tous les AUTRES joueurs qui ont Solar Absorbal
            players.filter { it.id != victim.id && it.alive && hasCard(it, "Solar Absorbal") }.forEach {
                it.gainEnergy(1)
            }

            // CARTE: Spiky Armor (Renvoie 1 dmg à l'attaquant)
            if (attacker != null && hasCard(victim, "Spiky Armor")) {
                // L'attaquant prend 1 dmg direct (sans réduction d'armure pour éviter boucle infinie simple)
                attacker.takeDamage(1)
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
