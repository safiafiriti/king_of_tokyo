package com.example.kingoftokyo.game.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kingoftokyo.game.engine.GameManager
import com.example.kingoftokyo.game.model.Card
import com.example.kingoftokyo.game.model.DiceSymbol
import com.example.kingoftokyo.game.model.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val gameManager = GameManager()

    // Damage taken by human this turn (for UI popup, shake, etc.)
    private val _humanHitDamage = MutableLiveData<Int?>()
    val humanHitDamage: LiveData<Int?> = _humanHitDamage

    private val _players = MutableLiveData<List<Player>>()
    val players: LiveData<List<Player>> = _players

    private var humanPlayerSelectedMonster: String? = null

    private val _dice = MutableLiveData<List<DiceSymbol>>()
    val dice: LiveData<List<DiceSymbol>> = _dice

    private val _currentPlayerIndex = MutableLiveData<Int>()
    val currentPlayerIndex: LiveData<Int> = _currentPlayerIndex

    private val _gameEnded = MutableLiveData<Boolean>()
    val gameEnded: LiveData<Boolean> = _gameEnded

    private val _winnerId = MutableLiveData<Int?>()
    val winnerId: LiveData<Int?> = _winnerId

    private val _rollsThisTurn = MutableLiveData<Int>()
    val rollsThisTurn: LiveData<Int> = _rollsThisTurn

    private val _diceLocked = MutableLiveData<List<Boolean>>()
    val diceLocked: LiveData<List<Boolean>> = _diceLocked

    private val _tokyoLeavePlayerId = MutableLiveData<Int?>()
    val tokyoLeavePlayerId: LiveData<Int?> = _tokyoLeavePlayerId

    // Boutique
    private val _marketCards = MutableLiveData<List<Card?>>()
    val marketCards: LiveData<List<Card?>> = _marketCards

    // Ouvrir/fermer le fragment "Cards"
    private val _showShopEvent = MutableLiveData<Boolean>()
    val showShopEvent: LiveData<Boolean> = _showShopEvent

    // Est-ce une phase d'achat de fin de tour ?
    private val _isBuyPhase = MutableLiveData<Boolean>(false)
    val isBuyPhase: LiveData<Boolean> = _isBuyPhase

    private val _roundNumber = MutableLiveData<Int>()
    val roundNumber: LiveData<Int> = _roundNumber

    // --- NOUVEAU : Exposer le max de lancers (pour l'UI) ---
    private val _maxRolls = MutableLiveData<Int>()
    val maxRolls: LiveData<Int> = _maxRolls

    // =========================================================
    // INIT
    // =========================================================

    fun startNewGame(playerNames: List<String>) {
        gameManager.initGame(playerNames)
        gameManager.startTurn()
        _rollsThisTurn.value = 0

        _marketCards.value = gameManager.marketCards.toList()
        _humanHitDamage.value = null

        refreshState()
    }

    fun startGameWithSelectedMonster(selectedMonster: String) {
        humanPlayerSelectedMonster = selectedMonster
        val monsters = listOf("rexmaw", "meko", "grumpjaw", "venomjunch")
        val remainingMonsters = monsters - selectedMonster

        // Human = first, others = CPU
        gameManager.initGame(listOf(selectedMonster) + remainingMonsters)
        gameManager.startTurn()
        _rollsThisTurn.value = 0
        _marketCards.value = gameManager.marketCards.toList()
        _humanHitDamage.value = null

        refreshState()
    }

    // =========================================================
    // SHOP
    // =========================================================

    // Bouton "Cards" (juste consulter)
    fun onShopPeekClicked() {
        _isBuyPhase.value = false
        _marketCards.value = gameManager.marketCards.toList()
        _showShopEvent.value = false
        _showShopEvent.value = true
    }

    fun onBuyCard(index: Int) {
        val player = gameManager.currentPlayer
        val success = gameManager.buyCard(player, index)
        if (success) {
            refreshState()
            _marketCards.value = gameManager.marketCards.toList()
        }
    }

    fun onCloseShop() {
        _showShopEvent.value = false

        // Si on était en phase d'achat de fin de tour → terminer le tour humain
        if (_isBuyPhase.value == true) {
            endTurnAndMaybePlayAi()
        }
    }

    // =========================================================
    // HUMAN ACTIONS
    // =========================================================

    fun onRollClicked() {
        // Empêcher de cliquer pendant un tour IA ou après la fin
        if (!gameManager.currentPlayer.isHuman || gameManager.gameEnded) return

        gameManager.rollDice()
        val current = _rollsThisTurn.value ?: 0
        _rollsThisTurn.value = current + 1
        refreshState()
    }

    fun onToggleDice(index: Int) {
        if (!gameManager.currentPlayer.isHuman || gameManager.gameEnded) return

        gameManager.toggleLock(index)
        refreshState()
    }

    fun onValidateClicked() {
        if (!gameManager.currentPlayer.isHuman || gameManager.gameEnded) return

        // Lock ALL the dice
        gameManager.lockAllDice()
        refreshState()

        // Resolve dice effects (damage, heal, energy, VP…)
        gameManager.resolveDice()
        refreshState()

        // Si un humain doit décider de quitter Tokyo → ouvrir la popup
        val pendingId = gameManager.pendingTokyoLeavePlayerId
        if (pendingId != null) {
            _tokyoLeavePlayerId.value = pendingId
            return
        }

        // Check fin de partie
        if (gameManager.gameEnded) {
            _gameEnded.value = true
            _winnerId.value = gameManager.winnerId
            return
        }

        // Phase d'achat de fin de tour pour l'humain
        _marketCards.value = gameManager.marketCards.toList()
        _isBuyPhase.value = true

        _showShopEvent.value = false
        _showShopEvent.value = true
    }

    fun onTokyoLeaveDecision(leave: Boolean) {
        gameManager.resolveTokyoLeaveChoice(leave)
        _tokyoLeavePlayerId.value = null

        if (gameManager.gameEnded) {
            _gameEnded.value = true
            _winnerId.value = gameManager.winnerId
            refreshState()
            return
        }

        // Après la décision Tokyo → fin de tour, puis IA éventuelle
        endTurnAndMaybePlayAi()
    }

    // =========================================================
    // TURN FLOW + AI
    // =========================================================

    // Finir le tour courant, passer au suivant, et lancer l'IA si besoin
    private fun endTurnAndMaybePlayAi() {
        gameManager.endTurn()
        gameManager.startTurn()
        _rollsThisTurn.value = 0
        refreshState()

        processAiIfNeeded()
    }

    // Si le joueur courant est une IA, on lui fait jouer un ou plusieurs tours
    private fun processAiIfNeeded() {
        if (gameManager.gameEnded) return
        if (gameManager.currentPlayer.isHuman) return

        // Si un humain doit décider de quitter Tokyo → l'IA s'arrête
        if (gameManager.pendingTokyoLeavePlayerId != null) {
            _tokyoLeavePlayerId.value = gameManager.pendingTokyoLeavePlayerId
            return
        }

        // Enchaîne les tours IA avec des delays pour qu'on les voie jouer
        viewModelScope.launch {
            var safety = 0
            while (
                !gameManager.gameEnded &&
                !gameManager.currentPlayer.isHuman &&
                gameManager.pendingTokyoLeavePlayerId == null &&
                safety < 10
            ) {
                playSingleAiTurn()
                safety++
            }
        }
    }

    // Un tour complet d'IA : roll 3x → lock → resolve → shop → endTurn
    private suspend fun playSingleAiTurn() {
        // On suppose que startTurn() a déjà été appelé pour cette IA

        // --- CORRECTION AI : L'IA doit aussi respecter son max de lancers ---
        val maxRolls = gameManager.maxRollsPerTurn
        var localRolls = 0

        while (localRolls < maxRolls && !gameManager.gameEnded) {
            gameManager.rollDice()
            refreshState()
            delay(600L)  // temps pour voir chaque roll

            localRolls++
        }

        // Lock visuellement
        gameManager.lockAllDice()
        refreshState()
        delay(400L)

        // Résoudre les dés
        gameManager.resolveDice()
        refreshState()
        delay(700L)

        // Si un humain doit décider de quitter Tokyo → stop ici
        val pendingId = gameManager.pendingTokyoLeavePlayerId
        if (pendingId != null) {
            _tokyoLeavePlayerId.value = pendingId
            return
        }

        // Fin de partie ?
        if (gameManager.gameEnded) {
            _gameEnded.value = true
            _winnerId.value = gameManager.winnerId
            refreshState()
            return
        }

        // IA achète une carte si possible
        val aiPlayer = gameManager.currentPlayer
        val cards = gameManager.marketCards
        for (i in cards.indices) {
            val c = cards[i] ?: continue
            if (aiPlayer.energy >= c.cost) {
                gameManager.buyCard(aiPlayer, i)
                break
            }
        }
        refreshState()
        delay(500L)

        // Fin du tour IA → joueur suivant
        gameManager.endTurn()
        gameManager.startTurn()
        _rollsThisTurn.value = 0
        refreshState()
        delay(500L)
    }

    // =========================================================
    // STATE REFRESH + HELPERS
    // =========================================================

    private fun refreshState() {
        _players.value = gameManager.players.toList()
        _roundNumber.value = gameManager.roundNumber
        _humanHitDamage.value = gameManager.humanHitDamageThisTurn
        _dice.value = gameManager.dice.map { it.symbol }
        _diceLocked.value = gameManager.dice.map { it.locked }
        _currentPlayerIndex.value = gameManager.currentPlayerIndex
        _gameEnded.value = gameManager.gameEnded
        _winnerId.value = gameManager.winnerId

        // --- NOUVEAU : Mettre à jour le max de lancers ---
        _maxRolls.value = gameManager.maxRollsPerTurn
    }

    fun getCurrentPlayerEnergy(): Int {
        val index = _currentPlayerIndex.value ?: 0
        return _players.value?.getOrNull(index)?.energy ?: 0
    }

    fun consumeHumanHitDamage() {
        gameManager.clearHumanHitDamage()
        _humanHitDamage.value = null
    }
}